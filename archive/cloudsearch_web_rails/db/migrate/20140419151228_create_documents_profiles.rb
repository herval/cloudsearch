class CreateDocumentsProfiles < ActiveRecord::Migration
  def change
    create_table :documents_profiles do |t|
      t.integer :profile_id
      t.integer :document_id
    end
  end
end
