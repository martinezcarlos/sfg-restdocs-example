package guru.springframework.sfgrestdocsexample.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.sfgrestdocsexample.domain.Beer;
import guru.springframework.sfgrestdocsexample.repositories.BeerRepository;
import guru.springframework.sfgrestdocsexample.web.model.BeerDto;
import guru.springframework.sfgrestdocsexample.web.model.BeerStyleEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.constraints.ConstraintDescriptions;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
@WebMvcTest(BeerController.class)
@ComponentScan(basePackages = "guru.springframework.sfgrestdocsexample.web.mappers")
class BeerControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockBean
  BeerRepository beerRepository;

  @Test
  void getBeerById() throws Exception {
    given(beerRepository.findById(any())).willReturn(Optional.of(Beer.builder().build()));

    final ConstrainedFields fields = new ConstrainedFields(BeerDto.class);

    mockMvc.perform(get("/api/v1/beer/{beerId}", UUID.randomUUID().toString())
        .accept(MediaType.APPLICATION_JSON)
        .param("isCold", "yes"))
        .andExpect(status().isOk())
        .andDo(document("v1/beer/find",
            pathParameters(
                parameterWithName("beerId").description("UUID of the desired beer to get.")
            ),
            requestParameters(
                parameterWithName("isCold").description("Is beer cold query param.")
            ),
            responseFields(
                fields.withPath("id").description("Id of the beer.").type(UUID.class),
                fields.withPath("version").description("Version of the beer.").type(Integer.class),
                fields.withPath("createdDate").description("Creation of the entry").type(OffsetDateTime.class),
                fields.withPath("lastModifiedDate").description("Last modification").type(OffsetDateTime.class),
                fields.withPath("beerName").description("Name of the beer.").type(String.class),
                fields.withPath("beerStyle").description("Style of the beer.").type(BeerStyleEnum.class),
                fields.withPath("upc").description("UPC of the beer.").type(Long.class),
                fields.withPath("price").description("Price of the beer.").type(BigDecimal.class),
                fields.withPath("quantityOnHand").description("Quantity on hand.").type(Integer.class)
            )
        ));
  }

  @Test
  void saveNewBeer() throws Exception {
    final BeerDto beerDto = getValidBeerDto();
    final String beerDtoJson = objectMapper.writeValueAsString(beerDto);

    final ConstrainedFields fields = new ConstrainedFields(BeerDto.class);

    mockMvc.perform(post("/api/v1/beer/")
        .contentType(MediaType.APPLICATION_JSON)
        .content(beerDtoJson))
        .andExpect(status().isCreated())
        .andDo(document("v1/beer/create",
            requestFields(
                fields.withPath("id").ignored(),
                fields.withPath("version").ignored(),
                fields.withPath("createdDate").ignored(),
                fields.withPath("lastModifiedDate").ignored(),
                fields.withPath("beerName").description("Name of the beer.").type(String.class),
                fields.withPath("beerStyle").description("Style of the beer.").type(BeerStyleEnum.class),
                fields.withPath("upc").description("UPC of the beer.").type(Long.class),
                fields.withPath("price").description("Price of the beer.").type(BigDecimal.class),
                fields.withPath("quantityOnHand").ignored()
            )
        ));
  }

  @Test
  void updateBeerById() throws Exception {
    final BeerDto beerDto = getValidBeerDto();
    final String beerDtoJson = objectMapper.writeValueAsString(beerDto);

    mockMvc.perform(put("/api/v1/beer/" + UUID.randomUUID().toString())
        .contentType(MediaType.APPLICATION_JSON)
        .content(beerDtoJson))
        .andExpect(status().isNoContent());
  }

  private BeerDto getValidBeerDto() {
    return BeerDto.builder()
        .beerName("Nice Ale")
        .beerStyle(BeerStyleEnum.ALE)
        .price(new BigDecimal("9.99"))
        .upc(123123123123L)
        .build();

  }

  private static class ConstrainedFields {

    private final ConstraintDescriptions constraintDescriptions;

    ConstrainedFields(final Class<?> input) {
      constraintDescriptions = new ConstraintDescriptions(input);
    }

    private FieldDescriptor withPath(final String path) {
      return fieldWithPath(path).attributes(key("constraints").value(StringUtils
          .collectionToDelimitedString(constraintDescriptions
              .descriptionsForProperty(path), ". ")));
    }
  }

}