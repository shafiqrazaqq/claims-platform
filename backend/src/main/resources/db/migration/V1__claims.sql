-- nvarchar keeps Chinese, Thai and other scripts, because claims come from several APAC markets.
create table claims (
    id               bigint identity(1, 1) primary key,
    claimant         nvarchar(100)  not null,
    market           nvarchar(2)    not null,
    type             nvarchar(20)   not null,
    policy_number    nvarchar(50)   not null,
    incident_date    date           not null,
    description      nvarchar(2000) not null,
    currency         nvarchar(3)    not null,
    estimated_amount decimal(14, 2) not null,
    reserve_amount   decimal(14, 2) not null,
    approved_amount  decimal(14, 2),
    status           nvarchar(20)   not null,
    assigned_officer nvarchar(100),
    decision_reason  nvarchar(2000),
    created_at       datetimeoffset not null,
    updated_at       datetimeoffset not null,
    version          bigint         not null
);

create index idx_claims_claimant on claims (claimant);
create index idx_claims_status on claims (status);

create table claim_activities (
    id         bigint identity(1, 1) primary key,
    claim_id   bigint        not null references claims (id),
    type       nvarchar(20)  not null,
    actor      nvarchar(100) not null,
    message    nvarchar(2000),
    created_at datetimeoffset not null
);

create index idx_activities_claim on claim_activities (claim_id);
