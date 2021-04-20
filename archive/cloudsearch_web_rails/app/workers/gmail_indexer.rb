class GmailIndexer < BaseIndexer
  include EncodingHelper
  sidekiq_options backtrace: true, retry: 1, unique: true, queue: :gmail

  def account_type
    GoogleAccount
  end

  def init_api_client
    Net::IMAP.new('imap.gmail.com', 993, usessl = true, certs = nil, verify = false)
  end

  def before_process
    @pool = Thread.pool(10)
  end

  def above_cutoff?
    true
  end

  def iterator
    sync_time = account[:metadata][:cutoff_date] || Time.now-10.years
    imap = api_client
    imap.authenticate('XOAUTH2', account.email, account.token)
    imap.select("INBOX") # TODO other folders
    imap.search(["SENTSINCE", sync_time.strftime("%d-%b-%Y")])
  end

  def process(message_index)
    @pool.process do
      msg = api_client.fetch(message_index, 'ENVELOPE')[0].attr["ENVELOPE"]
      message_id = msg.message_id
      existing = GmailEmail.where(account_id: account_id, message_id: message_id).first
      if !existing # emails don't change!
        full_msg = imap.fetch(i,'RFC822')[0].attr['RFC822']
        mail = Mail.new(full_msg)

        body = parse_body(mail)

        subject = strip_everything(mail.subject) rescue ""
        attachments = mail.attachments.collect(&:filename)
        attachments = nil if attachments.empty?

        from = parse_field(mail, :from)
        to = parse_field(mail, :to)
        reply_to = parse_field(mail, :reply_to)
        cc = parse_field(mail, :cc)

        GmailEmail.create(user_id: account.user_id,
                          account_id: account_id,
                          message_id: message_id,
                          subject: subject,
                          body: body,
                          from: from,
                          to: to,
                          reply_to: reply_to,
                          cc: cc,
                          sent_at: mail.date,
                          in_reply_to: mail.in_reply_to,
                          keywords: mail.keywords,
                          attachments: attachments,
                          folder: "INBOX")
        puts "Indexing message #{message_id}"

        if(service_account.metadata[:cutoff_date].nil? || mail.date > service_account.metadata[:cutoff_date])
          service_account.update_attributes(metadata: { cutoff_date: mail.date })
        end
      else
        puts "Skipping: #{message_id}"
      end

    end
  end

  #    # TODO delete deleted?
  #    #http://stackoverflow.com/questions/9956324/imap-synchronization

  def parse_field(mail, field)
    begin
      mail[field] && mail[field].formatted
    rescue NoMethodError => e
      puts e.message
      puts e.backtrace.join("\n")
      mail[field].to_s
    rescue => e
      puts e.message
      puts e.backtrace.join("\n")
      mail[field].to_s
    end
  end

end

  def parse_body(mail)
    if mail.multipart?
      # email is html and text version, get the first
      if mail.parts[0].content_type.start_with?('text/plain') && mail.parts[1] && mail.parts[1].content_type.start_with?("text/html")
        body = mail.parts[1].body.decoded
      else
        body = mail.parts[0].body.decoded
      end
      # TODO handle attachments
    else
      body = mail.body.decoded
    end

    body = strip_everything(body)
  end
