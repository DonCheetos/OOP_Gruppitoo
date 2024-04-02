# OOP_Gruppitöö

Teema: Chati programm

Liikmed: Maksim Kelus, Kaur Lõhmus, Rasmus Valk

Lühikirjeldus:
programm, mis võimaldab inimeselt inimesele ja inimeselt gruppile suhtlust, nii serveri vahendusel kui ka otse.

Klient saadab nii serverile kui sihtkasutajale sõnumi ja kui sihtkasutaja ei saanud kätte küsib ta serverilt hiljem
sõnumid salvestatakse faili nii servweris kui ka saajas
ajatempel sõnumitel
sümboliga tähised: kustutatud, loetud, ootel
kui saaja sõnumit näinud, saadab pingi serverile ja saatjale
kui saaja ei saa kätte, saadab saaja järgmised sõnumid kuni kättesaamiseni ainult serverile

klient refreshib iga kord kui saab uue sõnumi ja aegajalt pollib serverit.

kasutajaliides veebilehel mis jookseb kohalikus arvutis
ja suhtemiseks kasutab protokooli
1.klient1 ühendumisel saadab serverile oma kasutajanime, mille server seab IPga vastavusse map() abil, ja küsib serverilt map() andmebaasi, küsib saamata sõnumeid
2.klient1 saadab saadud map()-ist kasutajanimele vastavale IPle sõnumi
3klient1 ootab kui klient2 vastab, kui ei vasta saadab serverile.
4.2server salvestab sõnumi
5.2klient2 küsib serveriga ühendudes punkti 1 asju.
4.1klient2 salvestab saadud sõnumi ja vastab et sai kätte

hiljem laiendused:
failid
suurte failide proxystreamimine
logide kustutamine
kliendi recovery protokoll
