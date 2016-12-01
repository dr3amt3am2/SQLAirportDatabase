SELECT flightno1, flightno2, src, dst, dep_time, arr_time, layover, price, stops, seats
FROM((SELECT flightno flightno1, null flightno2, src, dst, dep_time, 
 			arr_time, seats, null layover, price, null stops
					FROM available_flights
					WHERE src = 'YEG' AND dst = 'LAX'
					AND dep_date = TO_DATE('22/12/2015', 'dd/mm/yyyy')
				) UNION
				(SELECT flightno1, flightno2, src, dst, dep_time,
						 arr_time, layover, price, 1 stops, seats  
					FROM good_connections
					WHERE src = 'YEG' AND dst = 'LAX' 
					AND dep_date = TO_DATE('22/12/2015', 'dd/mm/yyyy'))
				ORDER BY PRICE ASC
				);

-- fields desired:
-- flightno, src, dest, dep_time, arr_time, # of stops, layover time, 
-- price

-- good_connections for reference:  
create view good_connections (src,dst,dep_time,arr_time, flightno1,flightno2, 
	layover,price, dep_date, seats) as
  select a1.src, a2.dst, a1.dep_time, a2.arr_time, a1.flightno, a2.flightno, 
  (a2.dep_time-a1.arr_time)/24,
	a1.price+a2.price, a1.dep_date, (0.5*((a1.seats+a2.seats)-ABS(a1.seats-a2.seats)))
  from available_flights a1, available_flights a2
  where a1.dst=a2.src and a1.arr_time +1.5/24 <=a2.dep_time and 
  a1.arr_time +5/24 >=a2.dep_time;

-- available_flights fo reference:
  create view available_flights(flightno,dep_date, src,dst,dep_time,arr_time,fare,seats,
  	price) as 
  select f.flightno, sf.dep_date, f.src, f.dst, f.dep_time+(trunc(sf.dep_date)-trunc(f.dep_time)), 
	 f.dep_time+(trunc(sf.dep_date)-trunc(f.dep_time))+(f.est_dur/60+a2.tzone-a1.tzone)/24, 
         fa.fare, fa.limit-count(tno), fa.price
  from flights f, flight_fares fa, sch_flights sf, bookings b, airports a1, airports a2
  where f.flightno=sf.flightno and f.flightno=fa.flightno and f.src=a1.acode and
	f.dst=a2.acode and fa.flightno=b.flightno(+) and fa.fare=b.fare(+) and
	sf.dep_date=b.dep_date(+)
  group by f.flightno, sf.dep_date, f.src, f.dst, f.dep_time, f.est_dur,a2.tzone,
	a1.tzone, fa.fare, fa.limit, fa.price
  having fa.limit-count(tno) > 0;

  select flightno, dep_date, src, dst, to_char(dep_time,'HH24:MI'), 
         to_char(arr_time, 'HH24:MI'), fare, seats, price 
  from available_flights
  order by dep_date;