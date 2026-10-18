INSERT INTO template_watermarks (template_id, latest_version) VALUES
    ('11111111-1111-1111-1111-111111111111', 3),
    ('22222222-2222-2222-2222-222222222222', 2);

INSERT INTO engagement_versions (engagement_id, firm_id, template_id, applied_version, latest_known_version, status) VALUES
    ('aaaa1111-0000-0000-0000-000000000001', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 3, 3, 'current'),
    ('aaaa1111-0000-0000-0000-000000000002', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 2, 3, 'pending'),
    ('aaaa1111-0000-0000-0000-000000000003', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '22222222-2222-2222-2222-222222222222', 1, 2, 'pending'),
    ('bbbb2222-0000-0000-0000-000000000001', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '11111111-1111-1111-1111-111111111111', 2, 3, 'declined'),
    ('bbbb2222-0000-0000-0000-000000000002', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222', 2, 2, 'current');
