-- Default data for roles
INSERT INTO roles (role_name) VALUES
('ADMIN'),
('USER');

-- Default data for categories
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

-- Default data for users
INSERT INTO users (id, first_name, last_name, image, email, password, mobile, address, role_id) VALUES
-- Email: admin@webbook.com; Password: Admin12345
('10d24ab3-78b5-4306-b7cd-0a347fcb3f61', 'Admin', 'Webbook', 'https://res.cloudinary.com/dso24g1vf/image/upload/v1757683629/webbook/user_avatars/a4f7515c-f82b-4f5f-9902-b3f81810cf57.jpg', 'admin@webbook.com', '$2a$12$UVqOOEFFDPrj5sIgcY6hJ.NsDJabjcTAjxB7mWA/aCqW6X.vEpevK', '0123456789', '123 Admin Street, Ha Noi', 1),

-- Email: user1@webbook.com; Password: SampleUser111
('859016ef-a9a4-42db-ae55-f1313ffc7408', 'User1', 'Webbook', 'https://res.cloudinary.com/dso24g1vf/image/upload/v1757683654/webbook/user_avatars/859016ef-a9a4-42db-ae55-f1313ffc7408.jpg', 'user1@webbook.com', '$2a$12$8XvpZaT8sdVIbQgZfWwafuf2ijLsiQp.c3voc0Rgd0Vo4aPDtChLa', '0987654321', '456 User Avenue, Ho Chi Minh City', 2),

-- Email: user2@webbook.com; Password: SampleUser222
('a4f7515c-f82b-4f5f-9902-b3f81810cf57', 'User2', 'Webbook', 'https://res.cloudinary.com/dso24g1vf/image/upload/v1757683687/webbook/user_avatars/ac507745-9da0-4dd1-b427-d88769fe1a26.jpg', 'user2@webbook.com', '$2a$12$gj.WerRbs7fPsWFojbAoA.iiJI49xnB5hdtNzCRXE6ktgQxkuVPV.', '0369852147', '789 Mod Boulevard, Da Nang', 2),

-- Email: user3@webbook.com; Password: SampleUser333
('ac507745-9da0-4dd1-b427-d88769fe1a26', 'User3', 'Webbook', 'https://res.cloudinary.com/dso24g1vf/image/upload/v1757833964/webbook/user_avatars/501a3484-1ab3-4026-b1aa-d85dac278b8e.jpg', 'user3@webbook.com', '$2a$12$hi2Ga5ljg5YHjic4LeEy..0VTIfPXsDpxjjrHybs0aMPVtB0NeybK', '0147258369', '321 Reader Lane, Can Tho', 2);



