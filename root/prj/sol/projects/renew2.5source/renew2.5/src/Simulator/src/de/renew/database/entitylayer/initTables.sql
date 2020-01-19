create table STATE
(
	INITED              int           not null,
	RUNNING             int           not null
);


create table NET_INSTANCE
(
	NET_INSTANCE_ID     int           not null,
	NAME                varchar(64)   not null,
	DRAWING_OPEN        int           not null
);

create unique index NET_INSTANCE_PK_IDX on NET_INSTANCE
(
	NET_INSTANCE_ID
);

create index NET_INSTANCE_DO_IDX on NET_INSTANCE
(
	DRAWING_OPEN
);


create table TOKEN
(
	TOKEN_ID            int          not null,
	CLASS_NAME          varchar(128)  not null,
	SERIALISATION       blob          not null
);

create unique index TOKEN_PK_IDX on TOKEN
(
	TOKEN_ID
);


create table TOKEN_POSITION
(
	TOKEN_ID            int           not null,
	NET_INSTANCE_ID     int           not null,
	PLACE_INSTANCE_ID   varchar(128)  not null,
	QUANTITY            int           not null
);

create unique index TOKEN_POSITION_PK_IDX on TOKEN_POSITION
(
	TOKEN_ID,
	NET_INSTANCE_ID,
	PLACE_INSTANCE_ID
);
