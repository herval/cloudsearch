class CreateDocuments < ActiveRecord::Migration
  def change
    execute "CREATE EXTENSION IF NOT EXISTS hstore"

    create_table :documents do |t|
      t.string :name
      t.text :path
      t.text :content
      t.string :uid
      t.integer :account_id
      t.integer :parent_id
      t.integer :user_id
      t.json :metadata
      t.string :type
      t.timestamps
    end

    execute "ALTER TABLE documents ALTER COLUMN metadata SET DEFAULT '{}'"

    add_index :documents, :uid
    add_index :documents, :account_id
    add_index :documents, :user_id
  end
end
