create sequence if not exists hibernate_sequence start with 1 increment by 1;

create table if not exists SECURITY_GROUP ( id bigint not null, name varchar(255), primary key (id) );

create table if not exists SECURITY_GROUP_INTERFACE ( id bigint not null, name varchar(255), tag varchar(255), security_group_fk bigint, primary key (id) );

create table if not exists SECURITY_GROUP_INTERFACE_POLICY ( sgi_fk bigint not null, policy_fk bigint not null, primary key (sgi_fk, policy_fk));
 
create table if not exists DOMAIN ( id bigint not null, name varchar(255), primary key (id));

create table if not exists POLICY ( id bigint not null, name varchar(255), domain_fk bigint,primary key (id) );

create table if not exists DEVICE ( id bigint not null, name varchar(255), vsId bigint not null,primary key (id) );

create table if not exists DEVICE_MEMBER (id bigint not null, name varchar(255), device_fk bigint, primary key (id) );

alter table POLICY add constraint if not exists FK_PO_DOMAIN foreign key (domain_fk) references DOMAIN;

alter table DOMAIN add constraint if not exists DOMAIN_NAME UNIQUE (name);

alter table POLICY add constraint if not exists POLICY_NAME UNIQUE (name);

alter table DEVICE_MEMBER add constraint if not exists FK_DEVICE foreign key (device_fk) references DEVICE;

alter table DEVICE add constraint if not exists DEVICE_NAME UNIQUE (name);

alter table DEVICE_MEMBER add constraint if not exists DEVICEMEMBER_NAME UNIQUE (name);

alter table SECURITY_GROUP_INTERFACE add constraint if not exists FK_SECURITY_GROUP foreign key (security_group_fk) references SECURITY_GROUP;

alter table SECURITY_GROUP_INTERFACE_POLICY add constraint if not exists FK_SGI_POLICY_SGI foreign key (sgi_fk) references SECURITY_GROUP_INTERFACE;
                    
alter table SECURITY_GROUP_INTERFACE_POLICY add constraint if not exists FK_SGI_POLICY_POLICY foreign key (policy_fk) references POLICY;

