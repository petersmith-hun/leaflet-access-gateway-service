-- noinspection SqlNoDataSourceInspectionForFile

-- set constants
set @CREATED_DATE = '2017-12-14 20:00:00.000';
set @TEST_PW = 'testpw01';

insert into leaflet_roles (id, date_created, description, enabled, external_default, local_default, name, date_last_modified)
values (0x239EA48D10764DBAB8D1771BA5B5099F, '2026-01-31 21:07:40.508000', 'Account managed by external IDP, commenting possible, no direct control over own profile', true, true, false, 'External user', '2026-01-31 22:39:34.628000'),
       (0x2BA63E0DD3234857B3CCC8C6E8159A50, '2026-01-31 21:07:54.430000', 'Basic system access, control over own profile, commenting', true, false, true, 'User', '2026-01-31 22:39:42.853000'),
       (0xC5C02D326A6347C1A6950A680C84DFD7, '2026-01-31 21:06:34.747000', 'Full system access, all permission granted', true, false, false, 'Administrator', '2026-01-31 21:06:47.899000'),
       (0xE25E644444F04F218A850C0C4267D141, '2026-01-31 21:07:21.760000', 'Able to access blog management features, no special permissions', true, false, false, 'Editor', '2026-01-31 21:07:21.760000');

-- insert test users
insert into leaflet_users(id, date_created, is_enabled, default_locale, email, role_id, username, password, account_type)
values (1, @CREATED_DATE, true, 'EN', 'test-admin@ac-leaflet.local', 0xC5C02D326A6347C1A6950A680C84DFD7, 'Administrator', @TEST_PW, 'LOCAL'),
       (2, @CREATED_DATE, true, 'EN', 'test-user-1@ac-leaflet.local', 0x2BA63E0DD3234857B3CCC8C6E8159A50, 'Test User 1', @TEST_PW, 'LOCAL'),
       (3, @CREATED_DATE, true, 'EN', 'test-editor-2@ac-leaflet.local', 0xE25E644444F04F218A850C0C4267D141, 'Test Editor 2', @TEST_PW, 'LOCAL'),
       (4, @CREATED_DATE, true, 'HU', 'test-editor-3@ac-leaflet.local', 0xE25E644444F04F218A850C0C4267D141, 'Test Editor 3', @TEST_PW, 'LOCAL'),
       (5, @CREATED_DATE, true, 'HU', 'test-editor-pwgrant@ac-leaflet.local', 0xE25E644444F04F218A850C0C4267D141, 'Test Editor PW Grant', @TEST_PW, 'LOCAL'),
       (6, @CREATED_DATE, true, 'HU', 'test-user-github@ac-leaflet.local', 0x239EA48D10764DBAB8D1771BA5B5099F, 'Test External User GitHub', null, 'GITHUB'),
       (7, @CREATED_DATE, true, 'HU', 'test-user-google@ac-leaflet.local', 0x239EA48D10764DBAB8D1771BA5B5099F, 'Test External User Google', null, 'GOOGLE');

insert into leaflet_roles_permissions(role_id, permissions_id)
select
    (select id from leaflet_roles where name = 'Administrator') as role_id,
    id as permission_id
    from leaflet_permissions
    where name in ('read:comments:own', 'read:users:own', 'write:comments:own', 'write:users:own', 'read:categories',
                   'read:comments', 'read:documents', 'read:entries', 'write:tags', 'read:tags', 'write:categories',
                   'write:comments', 'write:documents', 'write:entries', 'read:users', 'read:admin', 'write:admin',
                   'write:users');

insert into leaflet_roles_permissions(role_id, permissions_id)
select
    (select id from leaflet_roles where name = 'Editor') as role_id,
    id as permission_id
from leaflet_permissions
where name in ('read:comments:own', 'read:users:own', 'write:comments:own', 'write:users:own', 'read:categories',
               'read:comments', 'read:documents', 'read:tags', 'read:entries', 'write:categories', 'write:comments',
               'write:documents', 'write:entries', 'write:tags');

insert into leaflet_roles_permissions(role_id, permissions_id)
select
    (select id from leaflet_roles where name = 'User') as role_id,
    id as permission_id
from leaflet_permissions
where name in ('read:users:own', 'write:comments:own', 'write:users:own', 'read:comments:own');

insert into leaflet_roles_permissions(role_id, permissions_id)
select
    (select id from leaflet_roles where name = 'External user') as role_id,
    id as permission_id
from leaflet_permissions
where name in ('read:users:own', 'write:comments:own', 'read:comments:own');
