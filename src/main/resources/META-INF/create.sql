 
create sequence if not exists hibernate_sequence start with 1 increment by 1;
 
create table if not exists DeviceMember (id bigint not null, name varchar(255), parent_id bigint, primary key (id) );
 
create table if not exists SecurityGroup ( id bigint not null, name varchar(255), policyId varchar(255), tag varchar(255), parent_id bigint, primary key (id) );
 
create table if not exists VSSDevice ( id bigint not null, name varchar(255), primary key (id) );
 
alter table DeviceMember add constraint if not exists FKfjf5y9rnm9q5vbxwkgmjaatoo foreign key (parent_id) references VSSDevice;
 
alter table SecurityGroup add constraint if not exists FK6c3solpbb0j6atgf5lxv3w6d7 foreign key (parent_id) references VSSDevice;