create sequence if not exists hibernate_sequence start with 1 increment by 1;

create table if not exists DeviceMember (id bigint not null, name varchar(255), parent_id bigint, primary key (id) );
 
create table if not exists SecurityGroup ( id bigint not null, name varchar(255), policyId varchar(255), tag varchar(255), parent_id bigint, primary key (id) );

create table if not exists SecurityGroupInterface ( id bigint not null, name varchar(255), policyId varchar(255), tag varchar(255));
 
create table if not exists VSSDevice ( id bigint not null, name varchar(255), primary key (id) );

create table if not exists DOMAIN ( id bigint not null, name varchar(255), primary key (id));

create table if not exists POLICY ( id bigint not null, name varchar(255), domain_fk bigint,primary key (id) );
 
alter table DeviceMember add constraint if not exists FK_DEVICE_MEMBER_VSS_DEVICE foreign key (parent_id) references VSSDevice;
 
alter table SecurityGroup add constraint if not exists FK_SECURITY_GROUP_VSS_DEVICE foreign key (parent_id) references VSSDevice;

alter table POLICY add constraint if not exists FK_PO_DOMAIN foreign key (domain_fk) references DOMAIN;

alter table DOMAIN add constraint DOMAIN_NAME UNIQUE (name);

alter table POLICY add constraint POLICY_NAME UNIQUE (name);

