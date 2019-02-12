
# 001 - Model de Dados

## Status
Aceito.


## Contexto

## Decisão

## Consequências


## Script SQL

    CREATE KEYSPACE radar_advisory WITH REPLICATION = {'class':'NetworkTopologyStrategy','dcband':2};
    use radar_advisory;


    CREATE TABLE ROUTES_FROM_DAY (
      evt_dt text,
      object_id text,
      customer_id text,
      lat float,
      lng float,
      update_at timestamp,
      last_check TIMESTAMP static,
      lost boolean,
      offline boolean,
      total_verify int,
      total_out int,
      PRIMARY KEY( (evt_dt), customer_id, object_id)
    )
     WITH comment='Supervised route from day'
     and CLUSTERING ORDER BY (customer_id asc, object_id asc);


    CREATE TABLE ROUTE_SUPERVISED (
      evt_dt text,
      object_id text,
      customer_id text,
      start_dt TIMESTAMP static,
      end_dt   TIMESTAMP static,
      last_check TIMESTAMP static,
      min_dist int static,
      radius_buffer float static,
      polygon_wkt_buf text static,
      mode_type text static,
      place text,
      lat float,
      lng float,
      local_type text,
      plan_order int,
      real_order int,
      minor_dist int,
      PRIMARY KEY( (customer_id,object_id,evt_dt), plan_order, place)
    )
     WITH comment='Supervised route tracker'
     and CLUSTERING ORDER BY (plan_order asc);
