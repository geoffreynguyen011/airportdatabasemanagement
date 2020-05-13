DROP TABLE IF EXISTS Customer CASCADE;--OK
DROP TABLE IF EXISTS Flight CASCADE;--OK
DROP TABLE IF EXISTS Pilot CASCADE;--OK
DROP TABLE IF EXISTS Plane CASCADE;--OK
DROP TABLE IF EXISTS Technician CASCADE;--OK

DROP TABLE IF EXISTS Reservation CASCADE;--OK
DROP TABLE IF EXISTS FlightInfo CASCADE;--OK
DROP TABLE IF EXISTS Repairs CASCADE;--OK
DROP TABLE IF EXISTS Schedule CASCADE;--OK

DROP SEQUENCE IF EXISTS plane_id CASCADE;
DROP SEQUENCE IF EXISTS pilot_id CASCADE;
DROP SEQUENCE IF EXISTS flight_num CASCADE;
DROP SEQUENCE IF EXISTS flighti_id CASCADE;
DROP SEQUENCE IF EXISTS tech_id CASCADE;
DROP SEQUENCE IF EXISTS res_num CASCADE;
DROP SEQUENCE IF EXISTS sched_id CASCADE;



-------------
---DOMAINS---
-------------
CREATE DOMAIN us_postal_code AS TEXT CHECK(VALUE ~ '^\d{5}$' OR VALUE ~ '^\d{5}-\d{4}$');
CREATE DOMAIN _STATUS CHAR(1) CHECK (value IN ( 'W' , 'C', 'R' ) );
CREATE DOMAIN _GENDER CHAR(1) CHECK (value IN ( 'F' , 'M' ) );
CREATE DOMAIN _CODE CHAR(2) CHECK (value IN ( 'MJ' , 'MN', 'SV' ) ); --Major, Minimum, Service
CREATE DOMAIN _PINTEGER AS int4 CHECK(VALUE > 0);
CREATE DOMAIN _PZEROINTEGER AS int4 CHECK(VALUE >= 0);
CREATE DOMAIN _YEAR_1970 AS int4 CHECK(VALUE >= 0);
CREATE DOMAIN _SEATS AS int4 CHECK(VALUE > 0 AND VALUE < 500);--Plane Seats

------------
---TABLES---
------------
CREATE TABLE Customer
(
	id INTEGER NOT NULL,
	fname CHAR(24) NOT NULL,
	lname CHAR(24) NOT NULL,
	gtype _GENDER NOT NULL,
	dob DATE NOT NULL,
	address CHAR(256),
	phone CHAR(10),
	zipcode char(10),
	PRIMARY KEY (id)
);

CREATE TABLE Pilot
(
	id INTEGER NOT NULL,
	fullname CHAR(128),
	nationality CHAR(24),
	PRIMARY KEY (id)
);

CREATE TABLE Flight
(
	fnum INTEGER NOT NULL,
	cost _PINTEGER NOT NULL,
	num_sold _PZEROINTEGER NOT NULL,
	num_stops _PZEROINTEGER NOT NULL,
	actual_departure_date DATE NOT NULL,
	actual_arrival_date DATE NOT NULL,
	arrival_airport CHAR(5) NOT NULL,-- AIRPORT CODE --
	departure_airport CHAR(5) NOT NULL,-- AIRPORT CODE --
	PRIMARY KEY (fnum)
);

CREATE TABLE Plane
(
	id INTEGER NOT NULL,
	make CHAR(32) NOT NULL,
	model CHAR(64) NOT NULL,
	age _YEAR_1970 NOT NULL,
	seats _SEATS NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE Technician
(
	id INTEGER NOT NULL,
	full_name CHAR(128) NOT NULL,
	PRIMARY KEY (id)
);

---------------
---RELATIONS---
---------------

CREATE TABLE Reservation
(
	rnum INTEGER NOT NULL,
	cid INTEGER NOT NULL,
	fid INTEGER NOT NULL,
	status _STATUS,
	PRIMARY KEY (rnum),
	FOREIGN KEY (cid) REFERENCES Customer(id),
	FOREIGN KEY (fid) REFERENCES Flight(fnum)
);

CREATE TABLE FlightInfo
(
	fiid INTEGER NOT NULL,
	flight_id INTEGER NOT NULL,
	pilot_id INTEGER NOT NULL,
	plane_id INTEGER NOT NULL,
	PRIMARY KEY (fiid),
	FOREIGN KEY (flight_id) REFERENCES Flight(fnum),
	FOREIGN KEY (pilot_id) REFERENCES Pilot(id),
	FOREIGN KEY (plane_id) REFERENCES Plane(id)
);

CREATE TABLE Repairs
(
	rid INTEGER NOT NULL,
	repair_date DATE NOT NULL,
	repair_code _CODE,
	pilot_id INTEGER NOT NULL,
	plane_id INTEGER NOT NULL,
	technician_id INTEGER NOT NULL,
	PRIMARY KEY (rid),
	FOREIGN KEY (pilot_id) REFERENCES Pilot(id),
	FOREIGN KEY (plane_id) REFERENCES Plane(id),
	FOREIGN KEY (technician_id) REFERENCES Technician(id)
);

CREATE TABLE Schedule
(
	id INTEGER NOT NULL,
	flightNum INTEGER NOT NULL,
	departure_time DATE NOT NULL,
	arrival_time DATE NOT NULL,
	PRIMARY KEY (id),
	FOREIGN KEY (flightNum) REFERENCES Flight(fnum)
);

----------------------------
-- INSERT DATA STATEMENTS --
----------------------------

COPY Customer (
	id,
	fname,
	lname,
	gtype,
	dob,
	address,
	phone,
	zipcode
)
FROM 'customer.csv'
WITH DELIMITER ',';

COPY Pilot (
	id,
	fullname,
	nationality
)
FROM 'pilots.csv'
WITH DELIMITER ',';

COPY Plane (
	id,
	make,
	model,
	age,
	seats
)
FROM 'planes.csv'
WITH DELIMITER ',';

COPY Technician (
	id,
	full_name
)
FROM 'technician.csv'
WITH DELIMITER ',';

COPY Flight (
	fnum,
	cost,
	num_sold,
	num_stops,
	actual_departure_date,
	actual_arrival_date,
	arrival_airport,
	departure_airport
)
FROM 'flights.csv'
WITH DELIMITER ',';

COPY Reservation (
	rnum,
	cid,
	fid,
	status
)
FROM 'reservation.csv'
WITH DELIMITER ',';

COPY FlightInfo (
	fiid,
	flight_id,
	pilot_id,
	plane_id
)
FROM 'flightinfo.csv'
WITH DELIMITER ',';

COPY Repairs (
	rid,
	repair_date,
	repair_code,
	pilot_id,
	plane_id,
	technician_id
)
FROM 'repairs.csv'
WITH DELIMITER ',';

COPY Schedule (
	id,
	flightNum,
	departure_time,
	arrival_time
)
FROM 'schedule.csv'
WITH DELIMITER ',';

--Sequences--
CREATE SEQUENCE plane_id START WITH 67;
CREATE SEQUENCE pilot_id START WITH 250;
CREATE SEQUENCE flight_num START WITH 2000;
CREATE SEQUENCE flighti_id START WITH 2000;
CREATE SEQUENCE tech_id START WITH 250;
CREATE SEQUENCE res_num START WITH 9999;
CREATE SEQUENCE sched_id START WITH 2000;

--Triggers and Procedures--
CREATE OR REPLACE FUNCTION inc_plane()
RETURNS trigger AS
$BODY$
BEGIN
	NEW.id = nextval('plane_id');
	RETURN NEW;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER update_plane BEFORE INSERT
ON Plane FOR EACH ROW
EXECUTE PROCEDURE inc_plane();

----------------------------------

CREATE OR REPLACE FUNCTION inc_pilot()
RETURNS trigger AS
$BODY$
BEGIN
	NEW.id = nextval('pilot_id');
	RETURN NEW;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER update_pilot BEFORE INSERT
ON Pilot FOR EACH ROW
EXECUTE PROCEDURE inc_pilot();

---------------------------------

CREATE OR REPLACE FUNCTION inc_flight()
RETURNS trigger AS
$BODY$
BEGIN
	NEW.fnum = nextval('flight_num');
	RETURN NEW;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER update_flight BEFORE INSERT
ON Flight FOR EACH ROW
EXECUTE PROCEDURE inc_flight();

---------------------------------

CREATE OR REPLACE FUNCTION inc_flightinfo()
RETURNS trigger AS
$BODY$
BEGIN
	NEW.fiid = nextval('flighti_id');
	RETURN NEW;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER update_flightinfo BEFORE INSERT
ON FlightInfo FOR EACH ROW
EXECUTE PROCEDURE inc_flightinfo();

--------------------------------

CREATE OR REPLACE FUNCTION inc_tech()
RETURNS trigger AS
$BODY$
BEGIN
	NEW.id = nextval('tech_id');
	RETURN NEW;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER update_tech BEFORE INSERT
ON Technician FOR EACH ROW
EXECUTE PROCEDURE inc_tech();

---------------------------------

CREATE OR REPLACE FUNCTION inc_res()
RETURNS trigger AS
$BODY$
BEGIN
	NEW.rnum = nextval('res_num');
	RETURN NEW;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER update_res BEFORE INSERT
ON Reservation FOR EACH ROW
EXECUTE PROCEDURE inc_res();

---------------------------------

CREATE OR REPLACE FUNCTION inc_sched()
RETURNS trigger AS
$BODY$
BEGIN
	NEW.id = nextval('sched_id');
	RETURN NEW;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER update_sched BEFORE INSERT
ON Schedule FOR EACH ROW
EXECUTE PROCEDURE inc_sched();
