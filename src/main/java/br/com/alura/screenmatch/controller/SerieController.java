package br.com.alura.screenmatch.controller;

import br.com.alura.screenmatch.dto.EpisodioDTO;
import br.com.alura.screenmatch.dto.SerieDTO;
import br.com.alura.screenmatch.service.SerieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/series")
public class SerieController {


    @Autowired
    private SerieService servico;

    @GetMapping
    public List<SerieDTO> ObterSeries() {
        return servico.ObterTodasAsSeries();
    }

    @GetMapping("/top5")
    public List<SerieDTO> ObterTop5Series() {
        return servico.ObterTop5Series();
    }

    @GetMapping("/lancamentos")
    public List<SerieDTO> obterLancamentos() {
        return servico.obterLancamento();
    }

    @GetMapping("/{id}")
    public SerieDTO ObterPorId(@PathVariable int id) {
        return servico.ObterPorId(id);
    }

    @GetMapping("/{id}/temporadas/todas")
    public List<EpisodioDTO> obterTodasTemporadas(@PathVariable long id) {
        return servico.ObterTodastemporadas(id);
    }

    @GetMapping("/{id}/temporadas/{numero}")
    public List<EpisodioDTO> obterTemporadasPorNumero(@PathVariable long id, @PathVariable Long numero) {
        return servico.obterTemporadasPorNumero(id,numero);
    }

    @GetMapping("/categoria/{nomeGenero}")
    public List<SerieDTO> obterSeriesPorCategoria(@PathVariable String nomeGenero) {
        return servico.obterSeriesPorCategoria(nomeGenero);
    }
}
