# # TODO save conversations (individual messages should not be searchable)
# class FacebookMessage < Document
#   field :from
#   field :body
#   field :chat_id
#   field :created_time
#
#   before_create :associate_profiles
#
#
#   private
#
#   def associate_profiles
#     self.profiles = [
#         Profile.update_or_create({ account: self.account,
#                          uid: from[:id] },
#                        {
#                          name: from[:name]
#                        })
#     ]
#   end
#
# end
