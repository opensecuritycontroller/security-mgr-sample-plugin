create sequence if not exists hibernate_sequence start with 1 increment by 1;

create table if not exists SecurityGroup ( id bigint not null, name varchar(255), policyId varchar(255), tag varchar(255), parent_id bigint, primary key (id) );

create table if not exists SecurityGroupInterface ( id bigint not null, name varchar(255), policyId varchar(255), tag varchar(255));
 
create table if not exists DOMAIN ( id bigint not null, name varchar(255), primary key (id));

create table if not exists POLICY ( id bigint not null, name varchar(255), domain_fk bigint,primary key (id) );

create table if not exists DEVICE ( id bigint not null, name varchar(255), vsId bigint not null,primary key (id) );

create table if not exists DEVICE_MEMBER (id bigint not null, name varchar(255), device_fk bigint, version varchar(255), rx bigint, txSva bigint, dropSva bigint, applianceIp varchar(255), applianceName varchar(255), managerIp varchar(255), brokerIp varchar(255), applianceGateway varchar(255), applianceSubnetMask varchar(255), publicIp varchar(255), primary key (id) );
 
alter table SecurityGroup add constraint if not exists FK_SECURITY_GROUP_VSS_DEVICE foreign key (parent_id) references VSSDevice;

alter table POLICY add constraint if not exists FK_PO_DOMAIN foreign key (domain_fk) references DOMAIN;

alter table DOMAIN add constraint if not exists DOMAIN_NAME UNIQUE (name);

alter table POLICY add constraint if not exists POLICY_NAME UNIQUE (name);

alter table DEVICE_MEMBER add constraint if not exists FK_DEVICE foreign key (device_fk) references DEVICE;

alter table DEVICE add constraint if not exists DEVICE_NAME UNIQUE (name);

alter table DEVICE_MEMBER add constraint if not exists DEVICEMEMBER_NAME UNIQUE (name);
