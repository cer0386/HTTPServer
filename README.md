# OSMZHTTPServer

### Soubory musí být nahrány do zařízení na paměťovou kartu do složky: `/sdcard/OSMZ`
### DEMO soubory pro aplikaci jsou ve složce `Data` 
### Testováno na **Pixel 2**

#### Stream funguje přes `/camera/snapshots`, v aplikaci jsou 2 tlačítka __Capture__ sloužící pro zahájení pořizování snímků a __Stop__ pro zastavení. 
#### Stream si lze prohlédnout na stránce stream.html, kde jsou také 2 tlačítka pro zahájení a ukončení prohlížení streamu.
#### CGI skripty lze prohlížet v prohlížeči po zadání `/cgi-bin/` a příkaz s argumenty oddělenými `%` 
##### Testováno na příkazech `/cgi-bin/uptime` a `/cgi-bin/cat%/proc/cpuinfo`

Nepodařilo se mi rozběhnout MJpeg a překlopit appku do podoby služby
Roman Černý, **CER0386**
