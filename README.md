# Documentation - The Secure Message

**    ![](https://lh5.googleusercontent.com/9JmpfzyTFzQe4hGHQ08IboVewophJ251oxbk1-iPlpIYbXIJG_QI9AmuT3FmjzL4MOrtpqlFECpyhV8W07VS4dOqynoWm6EcD7RUIXKEfDcsRRhulBIvuMoSd_XtuCjCKJa7qQdd)**

VANDERMAES Romuald,
DEMARET Romain,
MARCELLO Nicolas et
 VERMEIRE Lucas



# Client


## 1. Cient de base (version 1.0 - MR.Swinnen)

Le client version 1.0 est tout à fonctionnel pour l’utilisation de nos applications.

## 2. Client personnalisé (version 1.1)

Le client version 1.1 a une fonctionnalité supplémentaire.

A des fins de facilité lors de l'enregistrement de la clé OTP dans l'application d'authentification à double facteurs, lorsque le SIGNUP est validé, un QR code associé à la Clé OTP générée par le Client va s'afficher.

Toutefois, pour que le Client puisse être utilisé, il faut installer des librairies spécifiques, en plus de celles de base :

### Installations des librairies

**Clé OTP**

*pip install --user pyotp*

**QR Code**

*pip install --user qrcode*

*qrcode* est utilisé pour générer un Code QR sur base du lien OTP (fourni par pyotp)

**Image**

*pip install --user Image*

*Image* est utilisé pour ouvrir l’image (utilise le software par défaut du Système) du QR Code.

**![](https://lh4.googleusercontent.com/DUuCx_a6jgGSB3eT95Wy4CdYJMZq7BwWvpI_WIkgja8EA9xhHpk6IVlQ3T2SWtTQed_lmE20WozE5Nxnxe8XGbYEDbP9JQB7zTs6d6nXhBZza369NaXfo3Qdswq7Ncl0ZByL9rk7)**


# Serveur

## Configuration serveur

Dans notre application, chaque serveur a une configuration personnelle (fichier json). Nous avons créé 3 configurations pour 3 serveurs différents (server01, server02 et server03). Nos serveurs ont en paramètre une chaîne de caractères qui spécifie la configuration correspondante. S’il n’y a pas de paramètre, ce sera la configuration par défaut qui sera utilisée.(serverConfig.json)
  
Voici un exemple de configuration, celle du server01 (server01Config.json) :

**![](https://lh3.googleusercontent.com/V8lTgtxCu3tNtBsafGmfCXBJ1jUaQSQHZtI0wpd55RNSPyHb1iGy380q6LyN5oRcnxjO4tMxz43fH2d7QIKo8W7uP2CDYk0obDjnGZAWsh8ecS1zR_nZq13EP10xmJHB-UIGhgtN)**
Les configurations sont prévues pour la démonstration, y compris l’interaction avec le message gateway. Il faut donc éviter de les modifier totalement.

De plus, l’utilisateur doit **spécifier l’adresse ip de l’interface pour le multicast.**

***→ bind_netfwok_interface_adress : “192.168.133.11” (exemple)***
  
Pour cela, nous avons mis en place un programme indépendant au projet :

*ProgrameListNetInterface* permet de vérifier son adresse ip par rapport aux interfaces disponibles. De plus, les informations des interfaces seront affichées pour savoir quelle interface correspond à votre adresse ip.

Si votre adresse IP ou l’interface réseau n’est pas valide, le programme choisira la première interface valide pour que ça fonctionne malgré tout.

> **A savoir :** A l’école, il n’y a pas de communication entre le réseau wifi et le réseau par câble. Il peut avoir des pertes de
> message entre les deux. Pour éviter des soucis de multicast, il faut
> absolument spécifier l’interface correspondant.


Il est possible aussi de changer de serveur n’importe quand. L’utilisateur peut changer server_id. Il y a de nombreuses possibilités (*.group2.chat). Pour faciliter, l’expérience utilisateur de nos applications, nous vous conseillons de créer des configurations de lancement définies selon le serveur correspondant. Ci-dessous, nous avons un exemple de configuration de lancement du server01.

**![](https://lh6.googleusercontent.com/pjHhFhKS0EcAplAQL6LizBvpYqQojQvCdp5-Wd3nPWTtRezfVK_1UUIQIqML7ZFBgicrg33wv6IjyP1dNlx-u2dBeoidX8sTGA9wOMOmy8wHOoKZ5jrvtKCxvPXa-mDVcwBssROt)**

Ensuite, l’utilisateur doit générer une clé AES pour accéder au message gateway. Cette clé peut être généré grâce au programme ProgramAes128GcmKeyGenerator  indépendant à notre projet.  Cette clé doit être inscrite dans la configuration du serveur et du message gateway.

## Informations supplémentaires

Il est possible de modifier le statut de la vérification de la clé OTP. Cela permet de tester le programme avec une plus grande facilité.

Une mise en place par défaut des nos configurations serveur a été faite :

**![](https://lh6.googleusercontent.com/esmqXsK23bnOOHcQUwe4CGDvmOBkjL7tP7DlvEwKowK0OYLk1VPe35nAjajnrU9UIQ0L2rDa-e2KeUFk1srPLW31eMgsp2wmo-IyiKAuDdV7DCiOF6t-X_HMCepT_udu1piumZJf)**

Le  mgate_domain doit rester “group2.chat” pour notre application.

> **A savoir :** 
> Notre projet doit être lancée sous Java 10 pour permettre le bon fonctionnement de notre extension - Message offline


# Multicast

La documentation de notre Multicast est fort similaire à celle du serveur.

L’utilisateur doit **spécifier l’adresse ip de l’interface pour le multicast.**

***→ bind_netfwok_interface_adress : “192.168.133.11” (exemple)***

Le  mgate_domain doit rester “group2.chat” pour notre application.


# Message gateway

De nouveau, la documentation du Message gateway reste similaire au serveur et au Multicast.

Le  mgate_domain doit rester “group2.chat” pour notre application.

L’utilisateur doit **spécifier l’adresse ip de l’interface pour le multicast.**

***→ bind_netfwok_interface_adress : “192.168.133.11” (exemple)***

**![](https://lh4.googleusercontent.com/Tbf2tQcFR5UcMwOrBxKlM0jKClNL3XVno6-_y3ZA93mgMz3DSq9fQYhkaf7cNcynVTOUDGXsUS_57X-unuT5xaFqWJBRrWyloM5ibDLf9szSLNuzZLS3KxCsYrcTHF377OxXfpOV)**

Le message gateway démarre sur le port 58000. Ce port comme ceux des serveurs peut être modifier si besoin. De plus, il faut inscrire dans la configuration du message gateway pour chaque serveur la clé AES générée précédemment.

## Informations supplémentaires

L’utilisateur peut changer le temps d’interval pour l’annonceur pour le Multicast.

***→ “announce_interval” : 10 (exemple)***



## Extensions choisies

- Implémenter un client mobile Android ou iOS
- Mémoriser les messages off-line d’une manière sécurisée

***Projet principal***

-->https://git.cg.helmo.be/e160869/reseau_secMes


***Projet Extension - Client Android***

--> https://git.cg.helmo.be/e180416/SecMesAndroid


***Projet Client Python personnalisé***

--> https://git.cg.helmo.be/e160869/reseau_secMes_SwilaClient

***Documentation - The Secure Message (version docs)***

--> https://docs.google.com/document/d/1tVazhggZTUsAVRmlVg-F5dwdHEhYKwvLWpihVJ-B3Yc/edit?usp=sharing
