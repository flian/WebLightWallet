
CREATE TABLE `user_wallet`
(
    `id`            int(11) NOT NULL AUTO_INCREMENT,
    `username`      varchar(255) NOT NULL DEFAULT '',
    `coin_symbol`   varchar(255) NOT NULL,
    `wallet_key`    varchar(255) NOT NULL,
    `balance`       decimal(40, 10)       default 0,
    `locked_amount` decimal(40, 10)       default 0,
    `primary_address` varchar(255) NOT NULL,
    `encrypted_password`     varchar(255) DEFAULT '',
    `encrypted_pub_idx_key` varchar(255) DEFAULT '',
    PRIMARY KEY (`id`),
    UNIQUE KEY `wallet_key_unk` (`wallet_key`),
    UNIQUE KEY `user_name_coin_wallet_unk` (`username`,`coin_symbol`)
)

CREATE TABLE `user_coin_address`
(
    `id`            int(11) NOT NULL AUTO_INCREMENT,
    `label`         varchar(100) DEFAULT '',
    `wallet_key`    varchar(255) NOT NULL,
    `address`       varchar(255) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `rsa_private_pub_key`
(
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `private_key` text NOT NULL,
    `public_key` text NOT NULL,
    `idx_key` varchar(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `keys_idx_key_unk` (`idx_key`)
)