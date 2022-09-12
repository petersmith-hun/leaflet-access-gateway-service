-- noinspection SqlNoDataSourceInspectionForFile

-- set constants
set @CREATED_DATE = '2017-12-14 20:00:00.000';
set @TEST_PW = 'testpw01';

-- insert test users
insert into
    leaflet_users(id, date_created, is_enabled, default_locale, email, role, username, password, account_type)
values
    (1, @CREATED_DATE, true, 'EN', 'test-admin@ac-leaflet.local', 'ADMIN', 'Administrator', @TEST_PW, 'LOCAL'),
    (2, @CREATED_DATE, true, 'EN', 'test-user-1@ac-leaflet.local', 'USER', 'Test User 1', @TEST_PW, 'LOCAL'),
    (3, @CREATED_DATE, true, 'EN', 'test-editor-2@ac-leaflet.local', 'EDITOR', 'Test Editor 2', @TEST_PW, 'LOCAL'),
    (4, @CREATED_DATE, true, 'HU', 'test-editor-3@ac-leaflet.local', 'EDITOR', 'Test Editor 3', @TEST_PW, 'LOCAL'),
    (5, @CREATED_DATE, true, 'HU', 'test-editor-pwgrant@ac-leaflet.local', 'EDITOR', 'Test Editor PW Grant', @TEST_PW, 'LOCAL'),
    (6, @CREATED_DATE, true, 'HU', 'test-user-github@ac-leaflet.local', 'EXTERNAL_USER', 'Test External User GitHub', null, 'GITHUB'),
    (7, @CREATED_DATE, true, 'HU', 'test-user-google@ac-leaflet.local', 'EXTERNAL_USER', 'Test External User Google', null, 'GOOGLE');
