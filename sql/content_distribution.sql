-- confirm where content distributed
select info->>'serviceId' as serviceid, count(distinct(info->>'correlationId')) as items
from events
group by serviceid
