class CreateProfiles < ActiveRecord::Migration
  def change
    create_table :profiles do |t|
      t.string :uid
      t.string :name
      t.string :email
      t.integer :document_id
      t.integer :account_id
      t.integer :user_id
      t.json :metadata

      # t.string :username
      # t.string :picture
      # t.string :path
      # t.string :gender
      # t.string :locale
      # t.string :country

      t.timestamps
    end

    execute "ALTER TABLE profiles ALTER COLUMN metadata SET DEFAULT '{}'"


    add_index :profiles, :email
  end
end
