class AddAccountStste < ActiveRecord::Migration
  def change
    add_column :accounts, :state, :string, null: false, default: 'active'
  end
end
