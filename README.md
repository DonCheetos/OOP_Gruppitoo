# OOP_Gruppitöö

## Teema
Chati programm

## Liikmed
Maksim Kelus, Kaur Lõhmus, Rasmus Valk

## Lühikirjeldus
Programm võimaldab inimeselt inimesele ja inimeselt gruppile suhtlust, nii serveri vahendusel kui ka otse.

Klient saadab nii serverile kui sihtkasutajale sõnumi, kui sihtkasutaja ei saanud kätte, küsib ta serverilt hiljem.

Sõnumid salvestatakse faili nii serveris kui ka saajas.

Igal sõnumil on oma ajatempel.

Sümboliga tähised: kustutatud, loetud ja ootel.

Kui saaja on sõnumit näinud, saadab pingi serverile ja saatjale.

Kui saaja ei saa sõnumit kätte, saadab saatja järgmised sõnumid kuni kättesaamiseni ainult serverile.

Klient värskendab iga kord, kui saab uue sõnumi, ja aegajalt pollib serverit.

Kasutajaliides on veebilehel, mis jookseb kohalikus arvutis lisaks kliendile, ja suhtemiseks kasutab kindlat protokolli.

## Tööpõhimõte
1. Klient1 ühendumisel saadab Serverile oma kasutajanime, mille server seab IP-ga vastavusse paisktabeli (hashmap) abil, küsib serverilt paisktabeli ning küsib saamata sõnumeid.
2. Klient1 saadab saadud tebelist kasutajanimele vastavale IP-le sõnumi(d).
3. Klient1 ootab, kuni Klient2 vastab. Kui ei vastata, saadab sõnumi(d) serverile.
4. Server salvestab sõnumi(d), et tulevikus Klient2-le edasi saata.
5. Klient2 küsib kunagi Serveriga ühendudes punkti 1 sisu.
6. Klient2 salvestab saadud sõnumi(d) ja vastab, et sai need kätte.

## Hilisemad edasiarendused
1. Failide üle võrgu saatmine
2. Suurte failide proxystream-imine
3. Logide kustutamine
4. Kliendi recovery protokoll
