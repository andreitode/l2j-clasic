-- ----------------------------
-- Table structure for individualvotes
-- ----------------------------
DROP TABLE IF EXISTS `individualvotes`;
CREATE TABLE `individualvotes`  (
  `voterIp` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `voteSite` tinyint(3) NOT NULL,
  `DiffTime` bigint(20) NULL DEFAULT NULL,
  `votingTimeSite` bigint(20) NULL DEFAULT NULL,
  `alreadyRewarded` tinyint(3) NULL DEFAULT NULL,
  PRIMARY KEY (`voterIp`, `voteSite`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;