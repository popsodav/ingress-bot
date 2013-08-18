#!/bin/bash
cat ../cheat.log | grep "result" | tail -n 1 | python -mjson.tool > inv.json
echo "<html><head><meta http-equiv=\"refresh\" content=\"5; URL=http://ingress.bplaced.net/\"><title>Ingress Items 4 Free</title></head><body>">index.html
echo $(cat inv.json | grep -B 1 EMITTER_A | grep '"level": 1' | wc -l) "x L1 RESO<br>">>index.html
echo $(cat inv.json | grep -B 1 EMITTER_A | grep '"level": 2' | wc -l) "x L2 RESO<br>">>index.html
echo $(cat inv.json | grep -B 1 EMITTER_A | grep '"level": 3' | wc -l) "x L3 RESO<br>">>index.html
echo $(cat inv.json | grep -B 1 EMITTER_A | grep '"level": 4' | wc -l) "x L4 RESO<br>">>index.html
echo $(cat inv.json | grep -B 1 EMITTER_A | grep '"level": 5' | wc -l) "x L5 RESO<br>">>index.html
echo $(cat inv.json | grep -B 1 EMITTER_A | grep '"level": 6' | wc -l) "x L6 RESO<br>">>index.html
echo $(cat inv.json | grep -B 1 EMITTER_A | grep '"level": 7' | wc -l) "x L7 RESO<br>">>index.html
echo $(cat inv.json | grep -B 1 EMITTER_A | grep '"level": 8' | wc -l) "x L8 RESO<br>">>index.html
echo $(cat inv.json | grep -B 1 EMP_BURSTER | grep '"level": 1' | wc -l) "x L1 XMP<br>">>index.html
echo $(cat inv.json | grep -B 1 EMP_BURSTER | grep '"level": 2' | wc -l) "x L2 XMP<br>">>index.html
echo $(cat inv.json | grep -B 1 EMP_BURSTER | grep '"level": 3' | wc -l) "x L3 XMP<br>">>index.html
echo $(cat inv.json | grep -B 1 EMP_BURSTER | grep '"level": 4' | wc -l) "x L4 XMP<br>">>index.html
echo $(cat inv.json | grep -B 1 EMP_BURSTER | grep '"level": 5' | wc -l) "x L5 XMP<br>">>index.html
echo $(cat inv.json | grep -B 1 EMP_BURSTER | grep '"level": 6' | wc -l) "x L6 XMP<br>">>index.html
echo $(cat inv.json | grep -B 1 EMP_BURSTER | grep '"level": 7' | wc -l) "x L7 XMP<br>">>index.html
echo $(cat inv.json | grep -B 1 EMP_BURSTER | grep '"level": 8' | wc -l) "x L8 XMP<br>">>index.html
echo $(cat inv.json | grep -B 1 POWER_CUBE | grep '"level": 1' | wc -l) "x L1 CUBE<br>">>index.html
echo $(cat inv.json | grep -B 1 POWER_CUBE | grep '"level": 2' | wc -l) "x L2 CUBE<br>">>index.html
echo $(cat inv.json | grep -B 1 POWER_CUBE | grep '"level": 3' | wc -l) "x L3 CUBE<br>">>index.html
echo $(cat inv.json | grep -B 1 POWER_CUBE | grep '"level": 4' | wc -l) "x L4 CUBE<br>">>index.html
echo $(cat inv.json | grep -B 1 POWER_CUBE | grep '"level": 5' | wc -l) "x L5 CUBE<br>">>index.html
echo $(cat inv.json | grep -B 1 POWER_CUBE | grep '"level": 6' | wc -l) "x L6 CUBE<br>">>index.html
echo $(cat inv.json | grep -B 1 POWER_CUBE | grep '"level": 7' | wc -l) "x L7 CUBE<br>">>index.html
echo $(cat inv.json | grep -B 1 POWER_CUBE | grep '"level": 8' | wc -l) "x L8 CUBE<br>">>index.html
echo $(cat inv.json | grep MEDIA | wc -l) "x MEDIA<br>">>index.html
echo $(cat inv.json | grep -B 1 SHIELD | grep COMMON | wc -l) " x COMMON SHIELD<br>">>index.html
echo $(cat inv.json | grep -B 1 SHIELD | grep RARE | wc -l) " x RARE SHIELD<br>">>index.html
echo $(cat inv.json | grep -B 1 SHIELD | grep VERY_RARE | wc -l) " x VERY RARE SHIELD<br>">>index.html

echo $(cat inv.json | grep -B 1 HEATSINK | grep COMMON | wc -l) " x COMMON HEAT SINK<br>">>index.html
echo $(cat inv.json | grep -B 1 HEATSINK | grep RARE | wc -l) " x RARE HEAT SINK<br>">>index.html
echo $(cat inv.json | grep -B 1 HEATSINK | grep VERY_RARE | wc -l) " x VERY RARE HEAT SINK<br>">>index.html
echo $(cat inv.json | grep -B 1 MULTIHACK | grep COMMON | wc -l) " x COMMON MULTIHACK<br>">>index.html
echo $(cat inv.json | grep -B 1 MULTIHACK | grep RARE | wc -l) " x RARE MULTIHACK<br>">>index.html
echo $(cat inv.json | grep -B 1 MULTIHACK | grep VERY_RARE | wc -l) " x VERY RARE MULTIHACK<br>">>index.html
echo $(cat inv.json | grep FORCE_AMP | wc -l) "x FORCE AMP<br>">>index.html
echo $(cat inv.json | grep LINK_AMP | wc -l) "x LINK AMP<br>">>index.html
echo $(cat inv.json | grep TURRET | wc -l) "x TURRET<br>">>index.html
echo "</body></html>">>index.html
HOST=host.to.upload.to
username="user"
password=PASSWORD
file=index.html
ftp -n  $HOST > /dev/null << EOT
ascii
user $username $password
prompt
put index.html
bye
EOT
