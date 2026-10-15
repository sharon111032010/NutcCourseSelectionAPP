import json,re,sys
src,dst=sys.argv[1],sys.argv[2]
d=json.load(open(src))
def dept(n):
    m=re.match(r'^(.+?)(所專|所研|研|博|科?菁英班|技優)?[一二三四五六七]',n)
    return m.group(1) if m else n
out=[]
for c in d['courses']:
    p=c['parsed']
    out.append({
     'course_id':c['course_id'],'ccid':c['ccid'],'course_name':c['course_name'],
     'class_name':c['class_name'],'dept':dept(c['class_name']),'teacher':c['teacher'],
     'type':c['type'],'credits':c['credits'],'hours':c['hours'],'group':c['group'],
     'category':c['category'],'pe_activity':c['pe_activity'],'remark':c['remark'],
     'merged_note':c['merged_note'],'year_long':c['year_long'],'english_taught':c['english_taught'],
     'open_for_other_departments':c['open_for_other_departments'],'students':c['students'],
     'capacity_min':c['capacity_min'],'capacity_max':c['capacity_max'],
     'program':c['program'],'raw_time':c['raw']['time'],
     'sessions':p['sessions'],'no_schedule':p['no_schedule'],'parse_error':p['parse_error']})
json.dump({'semester':d['semester'],'updated_at':d['updated_at'],'count':len(out),'courses':out},
          open(dst,'w'),ensure_ascii=False,separators=(',',':'))
