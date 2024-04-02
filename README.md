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
1. saatja:ping käsk
2. saatja:protokolli kood
   3. saatja:sõnumi pikkus
   4. saatja:sõnum
   5. saaja:sain kätte
  
  kui kümne sekundi jooksul ei tule vastust, siis teine pool pole kättesaadav

  kui saab pingida, siis tuelmüürist läbi
  kui pingile vastab, siis saaja on online

hiljem laiendused:
failid
suurte failide proxystreamimine
logide kustutamine
kliendi recovery protokoll
