DROP TABLE IF EXISTS `clan_wars`;
CREATE TABLE IF NOT EXISTS `clan_wars` (
  `clan1` varchar(35) NOT NULL DEFAULT '',
  `clan2` varchar(35) NOT NULL DEFAULT '',
  `wantspeace1` decimal(1,0) NOT NULL DEFAULT '0',
  `wantspeace2` decimal(1,0) NOT NULL DEFAULT '0',
  `state` tinyint(4) NOT NULL DEFAULT 0,
  `war_state1` varchar(20) DEFAULT NULL,
  `war_state2` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`clan1`,`clan2`)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;