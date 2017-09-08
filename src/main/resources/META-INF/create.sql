create sequence if not exists hibernate_sequence start with 1 increment by 1;

create table if not exists SecurityGroup ( id bigint not null, name varchar(255), policyId varchar(255), tag varchar(255), parent_id bigint, primary key (id) );

create table if not exists SecurityGroupInterface ( id bigint not null, name varchar(255), policyId varchar(255), tag varchar(255));
 
create table if not exists DOMAIN ( id bigint not null, name varchar(255), primary key (id));

create table if not exists POLICY ( id bigint not null, name varchar(255), domain_fk bigint,primary key (id) );

create table if not exists DEVICE ( id bigint not null, name varchar(255), vsId bigint not null,primary key (id) );

create table if not exists MEMBERDEVICE (id bigint not null, name varchar(255), device_fk bigint, primary key (id) );
 
alter table SecurityGroup add constraint if not exists FK_SECURITY_GROUP_VSS_DEVICE foreign key (parent_id) references VSSDevice;

alter table POLICY add constraint if not exists FK_PO_DOMAIN foreign key (domain_fk) references DOMAIN;

alter table DOMAIN add constraint if not exists DOMAIN_NAME UNIQUE (name);

alter table POLICY add constraint if not exists POLICY_NAME UNIQUE (name);

alter table MEMBERDEVICE add constraint if not exists FK_DEVICE foreign key (device_fk) references DEVICE;

alter table DEVICE add constraint DEVICE_NAME UNIQUE (name);

alter table MEMBERDEVICE add constraint DEVICEMEMBER_NAME UNIQUE (name);

