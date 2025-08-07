use `xxl_job`;

ALTER TABLE xxl_job_group ADD app_secret varchar(128) NULL DEFAULT '' COMMENT '访问密钥' AFTER app_name;
ALTER TABLE xxl_job_group ADD CONSTRAINT app_name_unk UNIQUE KEY (app_name);