package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();

    private final String ENDERECO = "https://omdbapi.com/?t=";
    private final String API_KEY = "&apikey=d085ee2e";
    private List<DadosSerie> dadosSeries = new ArrayList<>();

    private SerieRepository repositorio;

    private List<Serie> series = new ArrayList<>();

    private Optional<Serie> serieBusca;
    public Principal(SerieRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void exibeMenu() {
        var opcao = -1;
        while(opcao != 0){
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries buscadas
                    4 - Buscar série por titulo
                    5 - Buscar séries por ator
                    6 - Top 5 séries
                    7 - Buscar séries por categoria
                    8 - Buscar por número de temporadas
                    9 - Buscar episodio por trecho
                    10 - Top 5 episodios
                    11 - Buscar episodios a partir de uma data
                    0 - Sair
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    ListarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriesPorAtor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscaeSeriePorCategoria();
                    break;
                case 8:
                    filtrarSeriesPorTemporadaEAvaliacao();
                    break;
                case 9:
                    buscarEpisodioPorTrecho();
                    break;
                case 10:
                    topEpisodiosPorSerie();
                    break;
                case 11:
                    buscarEpisodiosDepoisDeUmaData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }


    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        //dadosSeries.add(dados);
        repositorio.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie(){
        ListarSeriesBuscadas();
        System.out.println("Escolha uma Série pelo nome: ");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if(serie.isPresent()) {

            var serieEncontrada  = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());
            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        }else{
            System.out.println("Série não encontrada!");
        }
    }
    private void ListarSeriesBuscadas() {
        series = repositorio.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escolha uma Série pelo nome: ");
        var nomeSerie = leitura.nextLine();
        serieBusca = repositorio.findByTituloContainingIgnoreCase(nomeSerie);
        if(serieBusca.isPresent()) {
            System.out.println("Dados da série: " + serieBusca.get());
        }else{
            System.out.println("Série não encontrada!");
        }
    }
    private void buscarSeriesPorAtor() {
        System.out.println("Qual o nome do ator que deseja buscar: ");
        var nomeAtor = leitura.nextLine();
        System.out.println("A partir de qual avalaição deseja buscar: ");
        var avaliacao = leitura.nextDouble();
        List<Serie> seriesEncontradas = repositorio.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor,avaliacao);
        System.out.println("Séries encontradas com o "  +nomeAtor);
        seriesEncontradas.forEach(s ->
                System.out.println(s.getTitulo() + " Avaliação: " + s.getAvaliacao()));
    }
    private void buscarTop5Series() {
        List<Serie> serieTop = repositorio.findTop5ByOrderByAvaliacaoDesc();
        serieTop.forEach(s ->
                System.out.println(s.getTitulo() + " avaliação: " + s.getAvaliacao()));
    }

    private void buscaeSeriePorCategoria() {
        System.out.println("Qual a categoria que deseja buscar: ");
        var nomeGenero = leitura.nextLine();
        Categoria categoria = Categoria.fromPortugues(nomeGenero);
        List<Serie> seriesCategoria = repositorio.findByGenero(categoria);
        System.out.println("Séries encontradas de "  +categoria);
        seriesCategoria.forEach(s ->
                System.out.println(s.getTitulo() + " avaliação: " + s.getAvaliacao()));
    }

    private void filtrarSeriesPorTemporadaEAvaliacao(){
        System.out.println("Filtrar séries até quantas temporadas? ");
        var totalTemporadas = leitura.nextInt();
        leitura.nextLine();
        System.out.println("Com avaliação a partir de que valor? ");
        var avaliacao = leitura.nextDouble();
        leitura.nextLine();
        List<Serie> filtroSeries = repositorio.seriesPorTemporadaEAValiacao(totalTemporadas, avaliacao);
        System.out.println("*** Séries filtradas ***");
        filtroSeries.forEach(s ->
                System.out.println(s.getTitulo() + "  - avaliação: " + s.getAvaliacao()));
    }
    private void buscarEpisodioPorTrecho() {
        System.out.println("Qual o nome do episodio que deseja buscar: ");
        var trechoEpisodio = leitura.nextLine();
        List<Episodio> episodiosEncontrados = repositorio.episodioPorTrecho(trechoEpisodio);
        episodiosEncontrados.forEach(e ->
                System.out.printf("Série: %s Temporada %s - Episódio %s - %s\n",
                        e.getSerie().getTitulo(), e.getTemporada(),
                        e.getNumeroEpisodio(), e.getTitulo()));
    }

    private void topEpisodiosPorSerie() {
        buscarSeriePorTitulo();
        if(serieBusca.isPresent()){
            Serie serie = serieBusca.get();
            List<Episodio> topEpisodios = repositorio.topEpisodiosPorSerie(serie);
            topEpisodios.forEach(e ->
                    System.out.printf("Série: %s Temporada %s - Episódio %s - %s - %s\n",
                            e.getSerie().getTitulo(), e.getTemporada(),
                            e.getNumeroEpisodio(), e.getTitulo(),e.getAvaliacao()));
        }

    }


    private void buscarEpisodiosDepoisDeUmaData() {
        buscarSeriePorTitulo();
        if(serieBusca.isPresent()){
            Serie serie = serieBusca.get();
            System.out.println("Digite o ano limite do lançamento: ");
            var anoLancamento = leitura.nextInt();

            List<Episodio> episodiosAno = repositorio.episodiosPorSerieEAno(serie,anoLancamento);
            episodiosAno.forEach(System.out::println);
        }
    }

}


