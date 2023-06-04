CREATE TABLE room (
    number varchar(8) primary key,
    beds int not null
);

CREATE TABLE reservation (
    id varchar(36) primary key,
    room_number varchar(8) not null references room(number) ON DELETE CASCADE,
    client varchar(256) not null,
    check_in timestamp  not null,
    check_out timestamp not null
);

CREATE INDEX reservation_check_in ON reservation(check_in);
CREATE INDEX reservation_check_out ON reservation(check_out);