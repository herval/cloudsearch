class CreateAccounts < ActiveRecord::Migration
  def change
    enable_extension "hstore"

    create_table :accounts do |t|
      t.integer :user_id
      t.integer :profile_id
      t.string :service
      t.text :auth_code
      t.text :token
      t.text :refresh_token
      t.timestamp :token_expires_at
      t.text :secret
      t.string :uid
      t.string :email
      t.string :name
      t.timestamp :synced_at
      t.json :metadata
      t.string :type
      t.timestamps
    end

    execute "ALTER TABLE accounts ALTER COLUMN metadata SET DEFAULT '{}'"

  end
end
