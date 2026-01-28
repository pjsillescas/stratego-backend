
INSERT INTO player(id, username, password) VALUES
(1, 'testuser1', '$2a$10$eP3FFlxW0RYWE9lO2Lb/ZeD8aswR.g07IBxTmAabZJYP9vOi/6a.e'), -- password1
(2, 'testuser2', '$2a$10$4RPXI3HNETyZ04X4ko.KcuaoG6vSbifrTgWSzg7Yf7vFf9kipwn..'), -- password2
(3, 'testuser3', '$2a$10$Hv0G6KNRtt7EO.pnPNxdr.kpxoaDEH4JJxeeH1P9pSKlylI4HhN1e') -- password3
;

INSERT INTO game(id, name, creation_date, join_code, host, guest) VALUES
(1, 'testuser1s game', '2020-05-01T00:01:00Z', 'code1', 1, NULL),
(2, 'testuser2s game', '2020-05-01T00:02:00Z', 'code2', 2, NULL),
(3, 'testuser3s game', '2020-04-29T00:03:00Z', 'code3', 3, NULL),
(4, 'testuser1s game', '2020-05-01T20:04:00Z', 'code4', 1, 2),
(5, 'testuser2s game', '2020-05-01T00:05:00Z', 'code5', 2, 3)
;
