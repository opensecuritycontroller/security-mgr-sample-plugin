 
create sequence if not exists hibernate_sequence start with 1 increment by 1;

create table if not exists DeviceMember (id bigint not null, name varchar(255), parent_id bigint, primary key (id) );
 
create table if not exists SecurityGroup ( id bigint not null, name varchar(255), policyId varchar(255), tag varchar(255), parent_id bigint, primary key (id) );

create table if not exists SecurityGroupInterface ( id bigint not null, name varchar(255), policyId varchar(255), tag varchar(255));
 
create table if not exists VSSDevice ( id bigint not null, name varchar(255), primary key (id) );

create table if not exists DomainEntity ( Id bigint not null, name varchar(255), appliance_manager_connector_fk bigint, primary key (Id));

create table if not exists ApplianceManagerConnectorEntity ( id bigint not null, name varchar(255),primary key (id));

create table if not exists PolicyEntity ( id bigint not null, name varchar(255), domain_fk bigint,appliance_manager_connector_fk bigint,primary key (id) );
 
alter table DeviceMember add constraint if not exists FK_DEVICE_MEMBER_VSS_DEVICE foreign key (parent_id) references VSSDevice;
 
alter table SecurityGroup add constraint if not exists FK_SECURITY_GROUP_VSS_DEVICE foreign key (parent_id) references VSSDevice;

alter table PolicyEntity add constraint if not exists FK_PO_DOMAIN foreign key (domain_fk) references DomainEntity;

alter table PolicyEntity add constraint if not exists FK_PO_APPLIANCE_MANAGER_CONNECTOR foreign key (appliance_manager_connector_fk) references ApplianceManagerConnectorEntity;

alter table DomainEntity add constraint if not exists FK_DO_APPLIANCE_MANAGER_CONNECTOR foreign key (appliance_manager_connector_fk) references ApplianceManagerConnectorEntity;

alter table DomainEntity add constraint DOMAIN_NAME UNIQUE (name);

alter table PolicyEntity add constraint POLICY_NAME UNIQUE (name);

alter table ApplianceManagerConnectorEntity add constraint MANAGER_CONNECTOR_NAME UNIQUE (name);
