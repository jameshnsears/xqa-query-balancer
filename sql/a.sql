-- it's possible for shard to START / END before ingestbalancer END'd
select info->>'creationTime' as creationtime,
info->>'serviceId' as serviceid,
info->>'size' as size,
info->>'poolSize' as poolsize,
info->>'state' as state,
info->>'correlationId' as correlationid,
info->>'digest' as digest
from events
where info->>'correlationId' in (
select distinct(info->>'correlationId') as correlationid
from events
where info->>'source' like '%/xml/SP-MAIN-245-m0130-cm.xml%'
)
order by events.when asc;
