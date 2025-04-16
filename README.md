# SISD - Sistema Integrado Simulado de Distribuição

# Objetivo da atividade

- Desenvolver um sistema distribuído completo que simule uma plataforma de consulta e monitoramento de sensores climáticos remotos, utilizando os principais conceitos e técnicas de sistemas distribuídos.

# O que desenvolvemos

- O sistema possui diversos modelos de conexão e algoritmos distribuídos entre diferentes serviços, que estão 'rodando' em ambientes distintos (simulado a partir da utilização do Docker)

## Sockets

- Sockets é uma tecnologia utilizada para criar uma interface de comunicação entre dois processos, eles podem estar presentes na mesma máquina ou em lugares distintos, seja em um container, máquina virtual ou em algum lugar da internet.
- Por mais que a conexão ocorra somente entre dois processor por vez, é possível que um processo utilize uma porta via socket para ficar a espera de dados e diversos outros processos podem enviar dados nessa porta.
- O Sockets foi muito utilizado na aplicação, suas principais funções foram criar conexões entre os servidores e com o cliente (receptor dos dados sensoriais)
- O cliente criou um socket e todos os servidores enviam dados sensoriais periodicamente a cada 10 segundos (como foi citado, uma porta pode receber dados de diversos outros processos, porém não é possível outro processo ficar ouvindo na mesma porta)
- Para criar um Socket que vai receber os dados, basta você passar uma porta que não esta sendo utilizada para ele. Para enviar os dados via Socket o remetente tem que ter acesso ao IP e Porta do Socket destinatário.

### Usos

- Todos os servidores enviam dados periodicamente via sockets para o cliente
- O servidor líder envia dados periodicamente para os outros servidores, para saber se estão 'vivos'. Essa conexão é com sockets.
- Os servidores que não são líder enviam dados periodicamente para o líder, para saber se ele tá ativo. Essa conexão é com sockets.
- Quando um servidor inicia a eleição e tem um servidor com parâmetro maior que o dele, ele envia uma mensagem para o respectivo servidor iniciar uma eleição. Essa conexão é com sockets.

## gRPC

- gRPC é uma tecnologia criada pelo Google com intuito de facilitar a comunicação entre serviços distribuídos, é um framework que utilizado o Sockets como base.
- Sua utilização é dependente de arquivos .proto, que são utilizados para definir os métodos de comunicação e os dados que serão trafegados.
- Diante disso ele possuí uma tipagem forte e cria uma comunicação mais eficiente, pois é mais leve do que a transmissão de arquivos JSON ou XML.
- Além de utilizar de Sockets o gRPC é uma evolução das chamadas remotas, pois o que é gerado no .proto, nada mais é do que a definição de métodos que vão ser executados remotamente.

### Usos 

- Utilizamos o gRPC para criar um menu de funcionalidade, onde tem a opção de ver todos os dados recebidos de determinado servidor e tirar um snapshot do sistema.
- Esse menu roda individualmente, sendo um processo individual, quando o usuário digita no terminal a mensagem é enviada via gRPC para o Cliente, que retorna os dados requeridos.
- A ideia era que o cliente subisse em um docker, porém não foi possível criar conexão com a aplicação rodando em container, o Windowns dificulta o acesso para as portas.

## Multicast

- Multicast é uma tecnologia que também utiliza do sockets para que seja possível enviar uma mensagem para vários processor de uma vez e ao mesmo tempo.
- Quem tiver interesse em receber a mensagem, basta entrar no grupo multicast e qualquer mensagem enviada vai chegar para todo mundo.
- Além do multicast, existem o broadcast e o unicast, que são para enviar mensagens para todos da rede(mesmo que não esteja escrito) e enviar somente para um, respectivamente.

### Usos 

- O multicast foi utilizado para informar a todos os servidores quando um novo lider for eleito.
- Também foi utilizado para o servidor líder informar a todos os servidores quando algum servidor sair do ar.


