class CreateServiceAccounts < ActiveRecord::Migration
  def change
    create_table :service_accounts do |t|
      t.string :name
      t.string :url
      t.string :uid
      t.string :service
      t.json :metadata
      t.integer :account_id
    end

    execute "ALTER TABLE service_accounts ALTER COLUMN metadata SET DEFAULT '{}'"

    add_column :documents, :service_account_id, :integer
  end
end
