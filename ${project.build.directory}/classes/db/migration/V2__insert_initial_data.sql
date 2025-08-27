-- V2__Insert_sample_data.sql

-- Thêm dữ liệu mẫu cho roles
INSERT INTO roles (role_name) VALUES
('ADMIN'),
('USER'),
('MANAGER');

-- Thêm dữ liệu mẫu cho categories
INSERT INTO categories (name) VALUES
('Programming'),
('Fiction'),
('Science'),
('History'),
('Business'),
('Self-Help'),
('Romance'),
('Mystery'),
('Biography'),
('Technology');

-- 2. THÊM DỮ LIỆU MẪU VỚI UUID STRING
INSERT INTO users (id, username, image, email, password, mobile, address, role_id) VALUES
('10d24ab3-78b5-4306-b7cd-0a347fcb3f61', 'admin', 'https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.vecteezy.com%2Ffree-vector%2Fadmin-avatar&psig=AOvVaw25Nr1V_wIHI7Va-pLMpbGq&ust=1753801324868000&source=images&cd=vfe&opi=89978449&ved=0CBUQjRxqFwoTCOiCmejp344DFQAAAAAdAAAAABAL', 'admin@webbook.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0123456789', '123 Admin Street, Ha Noi', 1),
('859016ef-a9a4-42db-ae55-f1313ffc7408', 'user1', 'https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.freepik.com%2Ffree-photos-vectors%2Fadmin-avatar&psig=AOvVaw25Nr1V_wIHI7Va-pLMpbGq&ust=1753801324868000&source=images&cd=vfe&opi=89978449&ved=0CBYQjRxqFwoTCOiCmejp344DFQAAAAAdAAAAABAf', 'user1@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0987654321', '456 User Avenue, Ho Chi Minh City', 2),
('a4f7515c-f82b-4f5f-9902-b3f81810cf57', 'manager', 'https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.vecteezy.com%2Ffree-vector%2Fadmin-avatar&psig=AOvVaw25Nr1V_wIHI7Va-pLMpbGq&ust=1753801324868000&source=images&cd=vfe&opi=89978449&ved=0CBUQjRxqFwoTCOiCmejp344DFQAAAAAdAAAAABAp', 'mod@webbook.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0369852147', '789 Mod Boulevard, Da Nang', 3),
('ac507745-9da0-4dd1-b427-d88769fe1a26', 'bookworm', 'https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.freepik.com%2Ffree-photos-vectors%2Fsupervisor-avatar&psig=AOvVaw25Nr1V_wIHI7Va-pLMpbGq&ust=1753801324868000&source=images&cd=vfe&opi=89978449&ved=0CBUQjRxqFwoTCOiCmejp344DFQAAAAAdAAAAABAz', 'bookworm@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0147258369', '321 Reader Lane, Can Tho', 2);



