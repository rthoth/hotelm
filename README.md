# hotelm

Hotel Management.

Para executar o projeto é necessário definir as seguintes variáveis de ambiente:

* HOTELM_H2_FILE - O arquivo que será criado para armazenar o banco de dados.
* HOTELM_HOSTNAME - O hostname/interface onde o servidor http fará o binding.
* HOTELM_PORT - A porta que o servidor http escutará.

Exemplo:
```shell
env HOTELM_H2_FILE=/tmp/teste HOTELM_HOSTNAME=localhost HOTELM_PORT=8080 sbt run
```

## H2

Usei o h2 mais pela simplicidade, não precisar instalar nada. Como faz um tempo que eu não trabalhava com bancos de dados relacionais, optei por ele e também para experimentar o [ProtoQuill](https://github.com/zio/zio-protoquill).


## Endpoints

[Aqui](./Insomnia.json) está um arquivo do Insomnia com a coleção de endpoints que o hotelm suporta.