  select flightno1, flightno2, src, dst, layover, price 
  from (
  select flightno1, flightno2, src, dst, layover, price, row_number() over (order by price asc) rn 
  from 
  (select flightno1, flightno2, src, dst, layover, price
  from good_connections
  where to_char(dep_date,'DD/MM/YYYY')='22/12/2015' and src='YEG' and dst='LAX'
  union
  select flightno flightno1, '' flightno2, src, dst, 0 layover, price
  from available_flights
  where to_char(dep_date,'DD/MM/YYYY')='22/12/2015' and src='YEG' and dst='LAX'))
  where rn > 0;
