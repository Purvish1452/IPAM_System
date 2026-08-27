ALTER TABLE alert MODIFY alert_value LONGTEXT;

INSERT INTO alert (alert_key, alert_value)
VALUES
    ('newSubnetsDiscovered', 'true'),
    ('ipUtilizationFlag', 'true'),
    ('ipConflict', 'true');
