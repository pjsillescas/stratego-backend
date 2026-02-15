SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `player`;
CREATE TABLE `player`(
	id INTEGER NOT NULL AUTO_INCREMENT,
	username VARCHAR(128) NOT NULL UNIQUE,
	password VARCHAR(255) NOT NULL,
    CONSTRAINT player_pk PRIMARY KEY(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `game`;
CREATE TABLE `game`(
	id INTEGER NOT NULL AUTO_INCREMENT,
	name VARCHAR(128) NOT NULL,
	creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	join_code VARCHAR(128) NOT NULL,
	
	host INTEGER DEFAULT NULL,
	guest INTEGER DEFAULT NULL,

	phase VARCHAR(50),
	
    CONSTRAINT game_pk PRIMARY KEY(`id`),
	CONSTRAINT game_host_fk FOREIGN KEY(`host`) REFERENCES `player`(`id`),
	CONSTRAINT game_guest_fk FOREIGN KEY(`guest`) REFERENCES `player`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `stratego_status`;
CREATE TABLE `stratego_status` (
	id INTEGER NOT NULL AUTO_INCREMENT,
	game_id INTEGER NOT NULL,
	
	is_guest_turn INT(1) NOT NULL,

	board TEXT NOT NULL,
	
	is_host_initialized INT(1) NOT NULL,
	is_guest_initialized INT(1) NOT NULL,
	
	CONSTRAINT status_pk PRIMARY KEY(`id`),
	CONSTRAINT status_game_fk FOREIGN KEY(`game_id`) REFERENCES `game`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `stratego_movement`;
CREATE TABLE `stratego_movement` (
	id INTEGER NOT NULL AUTO_INCREMENT,
	game_id INTEGER NOT NULL,
	
	is_guest_turn INT(1) NOT NULL,

	rank_ VARCHAR(20) NOT NULL,
	row_initial INTEGER NOT NULL,
	col_initial INTEGER NOT NULL,
	row_final INTEGER NOT NULL,
	col_final INTEGER NOT NULL,
	
	result TEXT DEFAULT NULL,

	CONSTRAINT movement_pk PRIMARY KEY(`id`),
	CONSTRAINT movement_game_fk FOREIGN KEY(`game_id`) REFERENCES `game`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


SET FOREIGN_KEY_CHECKS = 1;


INSERT INTO player(id, username, password) VALUES
(1, 'user1', 'password1'),
(2, 'user2', 'password2'),
(3, 'user3', 'password3')
;

INSERT INTO game(id, name, creation_date, host, guest) VALUES
(1, 'user1s game', now(), 1, NULL),
(2, 'user2s game', now(), 2, NULL),
(3, 'user3s game', now(), 3, NULL),
(4, 'user1s game', now(), 1, 2),
(5, 'user2s game', now(), 2, 3)
;
