/*
  # Database Performance Indexes

  1. Index Strategy
    - Primary lookup columns (GUIDs, IDs)
    - Foreign key columns for JOINs
    - Status columns for filtering
    - Date columns for sorting
    - Search columns for text queries

  2. Index Types
    - Single column indexes for exact matches
    - Composite indexes for multi-column queries
    - Partial indexes for filtered queries

  3. Performance Impact
    - Faster WHERE clause execution
    - Improved JOIN performance
    - Optimized ORDER BY operations
    - Better pagination performance
*/

-- Correspondence table indexes
CREATE INDEX idx_correspondences_type_deleted_draft
    ON correspondences (correspondence_type_id, is_deleted, is_draft);

CREATE INDEX idx_correspondences_import_status
    ON correspondences (import_status);

CREATE INDEX idx_correspondences_creation_date
    ON correspondences (correspondence_creation_date);

CREATE INDEX idx_correspondences_last_modified
    ON correspondences (correspondence_last_modified_date);

CREATE INDEX idx_correspondences_coming_from
    ON correspondences (coming_from_guid);

CREATE INDEX idx_correspondences_classification
    ON correspondences (classification_guid);

CREATE INDEX idx_correspondences_creation_user
    ON correspondences (creation_user_guid);

CREATE INDEX idx_correspondences_creation_dept
    ON correspondences (creation_department_guid);

-- Correspondence Transactions indexes
CREATE INDEX idx_correspondence_transactions_doc_guid
    ON correspondence_transactions (doc_guid);

CREATE INDEX idx_correspondence_transactions_action_id
    ON correspondence_transactions (action_id);

CREATE INDEX idx_correspondence_transactions_migrate_status
    ON correspondence_transactions (migrate_status);

CREATE INDEX idx_correspondence_transactions_action_date
    ON correspondence_transactions (action_date);

CREATE INDEX idx_correspondence_transactions_last_modified
    ON correspondence_transactions (last_modified_date);

CREATE INDEX idx_correspondence_transactions_retry_count
    ON correspondence_transactions (retry_count);

-- Composite index for assignment queries (action_id = 12)
CREATE INDEX idx_correspondence_transactions_assignment
    ON correspondence_transactions (action_id, migrate_status, retry_count, last_modified_date);

-- Composite index for business log queries (action_id != 12)
CREATE INDEX idx_correspondence_transactions_business_log
    ON correspondence_transactions (migrate_status, retry_count, last_modified_date);

-- Correspondence Comments indexes
CREATE INDEX idx_correspondence_comments_doc_guid
    ON correspondence_comments (doc_guid);

CREATE INDEX idx_correspondence_comments_migrate_status
    ON correspondence_comments (migrate_status);

CREATE INDEX idx_correspondence_comments_comment_type
    ON correspondence_comments (comment_type);

CREATE INDEX idx_correspondence_comments_creation_date
    ON correspondence_comments (comment_creation_date);

CREATE INDEX idx_correspondence_comments_last_modified
    ON correspondence_comments (last_modified_date);

CREATE INDEX idx_correspondence_comments_retry_count
    ON correspondence_comments (retry_count);

-- Correspondence Attachments indexes
CREATE INDEX idx_correspondence_attachments_doc_guid
    ON correspondence_attachments (doc_guid);

CREATE INDEX idx_correspondence_attachments_import_status
    ON correspondence_attachments (import_status);

CREATE INDEX idx_correspondence_attachments_file_type
    ON correspondence_attachments (file_type);

CREATE INDEX idx_correspondence_attachments_is_primary
    ON correspondence_attachments (is_primary);

CREATE INDEX idx_correspondence_attachments_creation_date
    ON correspondence_attachments (file_creation_date);

-- Correspondence Copy To indexes
CREATE INDEX idx_correspondence_copy_tos_doc_guid
    ON correspondence_copy_tos (doc_guid);

CREATE INDEX idx_correspondence_copy_tos_import_status
    ON correspondence_copy_tos (import_status);

-- Correspondence Send To indexes
CREATE INDEX idx_correspondence_send_tos_doc_guid
    ON correspondence_send_tos (doc_guid);

CREATE INDEX idx_correspondence_send_tos_import_status
    ON correspondence_send_tos (import_status);

-- Correspondence Links indexes
CREATE INDEX idx_correspondence_links_doc_guid
    ON correspondence_links (doc_guid);

CREATE INDEX idx_correspondence_links_import_status
    ON correspondence_links (import_status);

-- Correspondence Current Users indexes
CREATE INDEX idx_correspondence_current_users_doc_guid
    ON correspondence_current_users (doc_guid);

CREATE INDEX idx_correspondence_current_users_import_status
    ON correspondence_current_users (import_status);

-- Correspondence Current Positions indexes
CREATE INDEX idx_correspondence_current_positions_doc_guid
    ON correspondence_current_positions (doc_guid);

CREATE INDEX idx_correspondence_current_positions_import_status
    ON correspondence_current_positions (import_status);

-- Correspondence Current Departments indexes
CREATE INDEX idx_correspondence_current_departments_doc_guid
    ON correspondence_current_departments (doc_guid);

CREATE INDEX idx_correspondence_current_departments_import_status
    ON correspondence_current_departments (import_status);

-- Correspondence Custom Fields indexes
CREATE INDEX idx_correspondence_custom_fields_doc_guid
    ON correspondence_custom_fields (doc_guid);

CREATE INDEX idx_correspondence_custom_fields_import_status
    ON correspondence_custom_fields (import_status);

-- Incoming Correspondence Migrations indexes
CREATE INDEX idx_incoming_migrations_correspondence_guid
    ON incoming_correspondence_migrations (correspondence_guid);

CREATE INDEX idx_incoming_migrations_current_phase
    ON incoming_correspondence_migrations (current_phase);

CREATE INDEX idx_incoming_migrations_overall_status
    ON incoming_correspondence_migrations (overall_status);

CREATE INDEX idx_incoming_migrations_phase_status
    ON incoming_correspondence_migrations (phase_status);

CREATE INDEX idx_incoming_migrations_creation_status
    ON incoming_correspondence_migrations (creation_status);

CREATE INDEX idx_incoming_migrations_assignment_status
    ON incoming_correspondence_migrations (assignment_status);

CREATE INDEX idx_incoming_migrations_business_log_status
    ON incoming_correspondence_migrations (business_log_status);

CREATE INDEX idx_incoming_migrations_comment_status
    ON incoming_correspondence_migrations (comment_status);

CREATE INDEX idx_incoming_migrations_closing_status
    ON incoming_correspondence_migrations (closing_status);

CREATE INDEX idx_incoming_migrations_is_need_to_close
    ON incoming_correspondence_migrations (is_need_to_close);

CREATE INDEX idx_incoming_migrations_retry_count
    ON incoming_correspondence_migrations (retry_count);

CREATE INDEX idx_incoming_migrations_last_modified
    ON incoming_correspondence_migrations (last_modified_date);

CREATE INDEX idx_incoming_migrations_created_doc_id
    ON incoming_correspondence_migrations (created_document_id);

-- Composite index for retryable migrations
CREATE INDEX idx_incoming_migrations_retryable
    ON incoming_correspondence_migrations (phase_status, retry_count, last_error_at);

-- Composite index for closing migrations
CREATE INDEX idx_incoming_migrations_closing
    ON incoming_correspondence_migrations (is_need_to_close, created_document_id, closing_status);

-- Correspondence Import Status indexes
CREATE INDEX idx_correspondence_import_status_guid
    ON correspondence_import_status (correspondence_guid);

CREATE INDEX idx_correspondence_import_status_overall
    ON correspondence_import_status (overall_status);

CREATE INDEX idx_correspondence_import_status_retry
    ON correspondence_import_status (retry_count, max_retries);

CREATE INDEX idx_correspondence_import_status_last_error
    ON correspondence_import_status (last_error_at);

-- Composite index for retryable imports
CREATE INDEX idx_correspondence_import_status_retryable
    ON correspondence_import_status (overall_status, retry_count, max_retries);

-- Users table indexes
CREATE INDEX idx_users_email
    ON users (email);

CREATE INDEX idx_users_login_name
    ON users (login_name);

CREATE INDEX idx_users_migrate_status
    ON users (migrate_status);

-- User Positions indexes
CREATE INDEX idx_user_positions_user_guid
    ON user_positions (user_guid);

CREATE INDEX idx_user_positions_pos_guid
    ON user_positions (pos_guid);

CREATE INDEX idx_user_positions_is_deleted
    ON user_positions (is_deleted);

-- Positions indexes
CREATE INDEX idx_positions_department_guid
    ON positions (department_guid);

CREATE INDEX idx_positions_is_manager
    ON positions (is_manager);

CREATE INDEX idx_positions_is_hidden
    ON positions (is_hidden);

-- Departments indexes
CREATE INDEX idx_departments_parent_guid
    ON departments (parent_guid);

CREATE INDEX idx_departments_main_parent_guid
    ON departments (main_parent_guid);

CREATE INDEX idx_departments_is_hidden
    ON departments (is_hidden);

-- Roles indexes
CREATE INDEX idx_roles_main_parent_guid
    ON roles (main_parent_guid);

-- Position Roles indexes
CREATE INDEX idx_pos_roles_role_guid
    ON pos_roles (role_guid);

CREATE INDEX idx_pos_roles_pos_guid
    ON pos_roles (pos_guid);

-- Classifications indexes
CREATE INDEX idx_classifications_is_hidden
    ON classifications (is_hidden);

-- Contacts indexes
CREATE INDEX idx_contacts_parent_guid
    ON contacts (parent_guid);

CREATE INDEX idx_contacts_is_blocked
    ON contacts (is_blocked);

-- Base Entity indexes (for all tables that extend BaseEntity)
CREATE INDEX idx_base_entity_creation_date_classifications
    ON classifications (creation_date);

CREATE INDEX idx_base_entity_last_modified_classifications
    ON classifications (last_modified_date);

CREATE INDEX idx_base_entity_migrate_status_classifications
    ON classifications (migrate_status);

CREATE INDEX idx_base_entity_creation_date_contacts
    ON contacts (creation_date);

CREATE INDEX idx_base_entity_last_modified_contacts
    ON contacts (last_modified_date);

CREATE INDEX idx_base_entity_migrate_status_contacts
    ON contacts (migrate_status);

CREATE INDEX idx_base_entity_creation_date_decisions
    ON decisions (creation_date);

CREATE INDEX idx_base_entity_last_modified_decisions
    ON decisions (last_modified_date);

CREATE INDEX idx_base_entity_migrate_status_decisions
    ON decisions (migrate_status);

CREATE INDEX idx_base_entity_creation_date_departments
    ON departments (creation_date);

CREATE INDEX idx_base_entity_last_modified_departments
    ON departments (last_modified_date);

CREATE INDEX idx_base_entity_migrate_status_departments
    ON departments (migrate_status);

CREATE INDEX idx_base_entity_creation_date_forms
    ON forms (creation_date);

CREATE INDEX idx_base_entity_last_modified_forms
    ON forms (last_modified_date);

CREATE INDEX idx_base_entity_migrate_status_forms
    ON forms (migrate_status);

CREATE INDEX idx_base_entity_creation_date_form_types
    ON form_types (creation_date);

CREATE INDEX idx_base_entity_last_modified_form_types
    ON form_types (last_modified_date);

CREATE INDEX idx_base_entity_migrate_status_form_types
    ON form_types (migrate_status);

CREATE INDEX idx_base_entity_creation_date_importance
    ON importance (creation_date);

CREATE INDEX idx_base_entity_last_modified_importance
    ON importance (last_modified_date);

CREATE INDEX idx_base_entity_migrate_status_importance
    ON importance (migrate_status);

CREATE INDEX idx_base_entity_creation_date_positions
    ON positions (creation_date);

CREATE INDEX idx_base_entity_last_modified_positions
    ON positions (last_modified_date);

CREATE INDEX idx_base_entity_migrate_status_positions
    ON positions (migrate_status);

CREATE INDEX idx_base_entity_creation_date_pos_roles
    ON pos_roles (creation_date);

CREATE INDEX idx_base_entity_last_modified_pos_roles
    ON pos_roles (last_modified_date);

CREATE INDEX idx_base_entity_migrate_status_pos_roles
    ON pos_roles (migrate_status);

CREATE INDEX idx_base_entity_creation_date_priority
    ON priority (creation_date);

CREATE INDEX idx_base_entity_last_modified_priority
    ON priority (last_modified_date);

CREATE INDEX idx_base_entity_migrate_status_priority
    ON priority (migrate_status);

CREATE INDEX idx_base_entity_creation_date_roles
    ON roles (creation_date);

CREATE INDEX idx_base_entity_last_modified_roles
    ON roles (last_modified_date);

CREATE INDEX idx_base_entity_migrate_status_roles
    ON roles (migrate_status);

CREATE INDEX idx_base_entity_creation_date_secrecy
    ON secrecy (creation_date);

CREATE INDEX idx_base_entity_last_modified_secrecy
    ON secrecy (last_modified_date);

CREATE INDEX idx_base_entity_migrate_status_secrecy
    ON secrecy (migrate_status);

CREATE INDEX idx_base_entity_creation_date_user_positions
    ON user_positions (creation_date);

CREATE INDEX idx_base_entity_last_modified_user_positions
    ON user_positions (last_modified_date);

CREATE INDEX idx_base_entity_migrate_status_user_positions
    ON user_positions (migrate_status);

CREATE INDEX idx_base_entity_creation_date_users
    ON users (creation_date);

CREATE INDEX idx_base_entity_last_modified_users
    ON users (last_modified_date);

CREATE INDEX idx_base_entity_migrate_status_users
    ON users (migrate_status);

-- Correspondence related entities indexes
CREATE INDEX idx_base_entity_creation_date_attachments
    ON correspondence_attachments (creation_date);

CREATE INDEX idx_base_entity_last_modified_attachments
    ON correspondence_attachments (last_modified_date);

CREATE INDEX idx_base_entity_migrate_status_attachments
    ON correspondence_attachments (migrate_status);

CREATE INDEX idx_base_entity_creation_date_comments
    ON correspondence_comments (creation_date);

CREATE INDEX idx_base_entity_last_modified_comments
    ON correspondence_comments (last_modified_date);

CREATE INDEX idx_base_entity_migrate_status_comments
    ON correspondence_comments (migrate_status);

CREATE INDEX idx_base_entity_creation_date_copy_tos
    ON correspondence_copy_tos (creation_date);

CREATE INDEX idx_base_entity_last_modified_copy_tos
    ON correspondence_copy_tos (last_modified_date);

CREATE INDEX idx_base_entity_migrate_status_copy_tos
    ON correspondence_copy_tos (migrate_status);

CREATE INDEX idx_base_entity_creation_date_current_departments
    ON correspondence_current_departments (creation_date);

CREATE INDEX idx_base_entity_last_modified_current_departments
    ON correspondence_current_departments (last_modified_date);

CREATE INDEX idx_base_entity_migrate_status_current_departments
    ON correspondence_current_departments (migrate_status);

CREATE INDEX idx_base_entity_creation_date_current_positions
    ON correspondence_current_positions (creation_date);

CREATE INDEX idx_base_entity_last_modified_current_positions
    ON correspondence_current_positions (last_modified_date);

CREATE INDEX idx_base_entity_migrate_status_current_positions
    ON correspondence_current_positions (migrate_status);

CREATE INDEX idx_base_entity_creation_date_current_users
    ON correspondence_current_users (creation_date);

CREATE INDEX idx_base_entity_last_modified_current_users
    ON correspondence_current_users (last_modified_date);

CREATE INDEX idx_base_entity_migrate_status_current_users
    ON correspondence_current_users (migrate_status);

CREATE INDEX idx_base_entity_creation_date_custom_fields
    ON correspondence_custom_fields (creation_date);

CREATE INDEX idx_base_entity_last_modified_custom_fields
    ON correspondence_custom_fields (last_modified_date);

CREATE INDEX idx_base_entity_migrate_status_custom_fields
    ON correspondence_custom_fields (migrate_status);

CREATE INDEX idx_base_entity_creation_date_links
    ON correspondence_links (creation_date);

CREATE INDEX idx_base_entity_last_modified_links
    ON correspondence_links (last_modified_date);

CREATE INDEX idx_base_entity_migrate_status_links
    ON correspondence_links (migrate_status);

CREATE INDEX idx_base_entity_creation_date_send_tos
    ON correspondence_send_tos (creation_date);

CREATE INDEX idx_base_entity_last_modified_send_tos
    ON correspondence_send_tos (last_modified_date);

CREATE INDEX idx_base_entity_migrate_status_send_tos
    ON correspondence_send_tos (migrate_status);

-- Text search indexes for common search patterns
CREATE INDEX idx_correspondences_subject_text
    ON correspondences (subject(100));

CREATE INDEX idx_correspondences_reference_no
    ON correspondences (reference_no);

CREATE INDEX idx_correspondences_external_reference
    ON correspondences (external_reference_number);

CREATE INDEX idx_correspondence_transactions_from_user
    ON correspondence_transactions (from_user_name(50));

CREATE INDEX idx_correspondence_transactions_to_user
    ON correspondence_transactions (to_user_name(50));

CREATE INDEX idx_correspondence_transactions_action_english
    ON correspondence_transactions (action_english_name(50));

CREATE INDEX idx_correspondence_comments_creation_user
    ON correspondence_comments (creation_user_guid);

-- Composite indexes for complex queries used in repositories
CREATE INDEX idx_correspondence_transactions_search
    ON correspondence_transactions (migrate_status, action_id, last_modified_date);

CREATE INDEX idx_correspondence_comments_search
    ON correspondence_comments (migrate_status, comment_type, last_modified_date);

CREATE INDEX idx_incoming_migrations_search
    ON incoming_correspondence_migrations (overall_status, current_phase, last_modified_date);

-- Performance optimization for JOIN queries
CREATE INDEX idx_user_dept_lookup
    ON user_positions (user_guid, pos_guid);

CREATE INDEX idx_pos_dept_lookup
    ON positions (guid, department_guid);

-- Indexes for statistics queries
CREATE INDEX idx_stats_correspondence_transactions_action_status
    ON correspondence_transactions (action_id, migrate_status);

CREATE INDEX idx_stats_correspondence_comments_status
    ON correspondence_comments (migrate_status);

CREATE INDEX idx_stats_incoming_migrations_phase_status
    ON incoming_correspondence_migrations (current_phase, overall_status);

-- Indexes for date range queries
CREATE INDEX idx_correspondences_date_range
    ON correspondences (correspondence_creation_date, correspondence_last_modified_date);

CREATE INDEX idx_correspondence_transactions_date_range
    ON correspondence_transactions (action_date, last_modified_date);

CREATE INDEX idx_correspondence_comments_date_range
    ON correspondence_comments (comment_creation_date, last_modified_date);