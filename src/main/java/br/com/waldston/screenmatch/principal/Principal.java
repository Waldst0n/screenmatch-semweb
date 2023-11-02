package br.com.waldston.screenmatch.principal;

import br.com.waldston.screenmatch.model.DadosEpisodio;
import br.com.waldston.screenmatch.model.DadosSerie;
import br.com.waldston.screenmatch.model.DadosTemporada;
import br.com.waldston.screenmatch.model.Episodio;
import br.com.waldston.screenmatch.service.ConsumoApi;
import br.com.waldston.screenmatch.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
     private Scanner scanner = new Scanner(System.in);
     private ConsumoApi consumo = new ConsumoApi();
     private ConverteDados conversor = new ConverteDados();

     private  final String ENDERECO = "https://www.omdbapi.com/?t=";
     private  final String API_KEY = "&apikey=b8f2f728";

    public void ExibeMenu() {
        System.out.println("Digite o nome da série para busca");

        var nomeSerie = scanner.nextLine();

        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ","+") + API_KEY);

        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);


        List<DadosTemporada> temporadas = new ArrayList<>();

		for (int i = 1; i< dados.totalTemporadas(); i++) {

			json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ","+") + "&season=" + i + API_KEY);
			DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
			temporadas.add(dadosTemporada);

		}
		temporadas.forEach(System.out::println);


        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));


        List<String> nomes = Arrays.asList("Tom", "Jane", "Alana", "Pedro");

        nomes.stream()
                .sorted()
                .limit(3)
                .filter(n -> n.startsWith("A"))
                .map(n -> n.toUpperCase())
                .forEach(System.out::println);


        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());

        System.out.println("Top 5 episódios");
        dadosEpisodios.stream()
                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
                .peek(e -> System.out.println("Primeiro filtro N/A " + e))
                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
                .peek(e -> System.out.println("Ordenação " + e))
                .limit(10)
                .peek(e -> System.out.println("Limit " + e))
                .map(e -> e.titulo().toUpperCase())
                .peek(e -> System.out.println("Map " + e))
                .forEach(System.out::println);

        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numeroTemporada(), d)))
                .collect(Collectors.toList());

        episodios.forEach(System.out::println);


        System.out.println("Digite um trecho do titulo do episodio");
        var trechoTitulo = scanner.nextLine();
        Optional<Episodio> episodioBuscado = episodios.stream()
                .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
                .findFirst();

        if (episodioBuscado.isPresent()) {
            System.out.println("Episodio encontrado");
            System.out.println("Temporada" + episodioBuscado.get());
        } else {
            System.out.println("Episodio não encontrado");
        }

        System.out.println("A partir de que ano voce deseja ver os episódios?");
        var ano = scanner.nextInt();
        scanner.nextLine();

        LocalDate dataBusca = LocalDate.of(ano,1,1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        episodios.stream()
                .filter(e -> e.getDataLancamento()!= null && e.getDataLancamento().isAfter(dataBusca))
                .forEach(e -> System.out.println(
                        " Temporada: " + e.getTemporada() +
                                " Episodio: " + e.getTitulo() +
                                " Data lançamento: " + e.getDataLancamento().format(formatter)
                ));


        Map<Integer, Double> avaliacaoPorTemporada = episodios.stream()
                .filter(e -> e.getAvaliacaoEpisodio() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getAvaliacaoEpisodio)));

        System.out.println(avaliacaoPorTemporada);


        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getAvaliacaoEpisodio() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacaoEpisodio));

        System.out.println("media " + est.getAverage());
        System.out.println( "Melhor episódio " + est.getMax());
        System.out.println("Pior episódio " + est.getMin());
        System.out.println("Quantidade " + est.getCount());




    }


}
