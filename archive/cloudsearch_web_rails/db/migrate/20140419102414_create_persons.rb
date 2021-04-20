class CreatePersons < ActiveRecord::Migration
  def change
    create_table :persons do |t|
      t.string :picture
      t.string :description
      t.integer :user_id
      t.integer :account_id
      t.timestamps
    end

    add_column :persons, :emails, :string, array: true, default: '{}'
    add_column :profiles, :person_id, :integer
    add_column :documents, :person_id, :integer
  end
end
