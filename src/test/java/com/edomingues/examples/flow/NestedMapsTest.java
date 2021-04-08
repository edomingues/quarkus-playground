package com.edomingues.examples.flow;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.reactivestreams.FlowAdapters;

import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Publisher;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.reactivestreams.FlowAdapters.toFlowPublisher;
import static org.reactivestreams.FlowAdapters.toPublisher;


class NestedMapsTest {

    static record Pet(String name) {}

    static record Collar(String tag) {}

    static class PetRepository {

        Publisher<Pet> searchPets(String search) {
            return toFlowPublisher(Multi.createFrom().items(new Pet("dog"), new Pet("cat"), new Pet("bird")));
        }

        Publisher<Pet> save(Pet pet) {
            return toFlowPublisher(Multi.createFrom().item(pet));
        }
    }

    static class PetService {

        Publisher<Collar> getCollarForMyPet(Pet pet) {
            return toFlowPublisher(Multi.createFrom().item(new Collar("Collar of " + pet.name)));
        }

    }

    static class PetConverter {
        static Pet addCollarToPet(Pet pet, Collar collar) {
            return new Pet(pet.name + " with " + collar.tag);
        }
    }

    static PetRepository petRepository = new PetRepository();
    static PetService petService = new PetService();

    static Publisher<Pet> searchPetsNested(String petSearch) {
        return toFlowPublisher(Multi.createFrom().publisher(toPublisher(
                petRepository.searchPets(petSearch))) // returns Publisher<Pet>
                 .onItem().transformToUniAndMerge(
                pet ->
                    Uni.createFrom().publisher(toPublisher(
                        petService.getCollarForMyPet(pet))) // returns Publisher<Collar>
                       .map(
                           collar ->
                               PetConverter.addCollarToPet(pet, collar) // returns Pet (with Collar)
                           )));
    }

    static Publisher<Pet> searchPetsZipTuple(String petSearch) {
        return toFlowPublisher(Multi.createFrom().publisher(toPublisher(
            petRepository.searchPets(petSearch))) // returns Publisher<Pet>
                                    .onItem().transformToUniAndMerge(
                pet ->
                    Uni.combine().all().unis(Uni.createFrom().item(pet),
                                             Uni.createFrom().publisher(toPublisher(petService.getCollarForMyPet(pet))))
                       .asTuple()) // returns Publisher<Tuple<Pet,Collar>>
                                    .map(
                                        tuple ->
                                            PetConverter.addCollarToPet(tuple.getItem1(), tuple.getItem2()))); // returns Pet (with Collar)
    }

    static Publisher<Pet> searchPetsZipRecord(String petSearch) {
        return toFlowPublisher(Multi.createFrom().publisher(toPublisher(petRepository.searchPets(petSearch))) // returns Publisher<Pet>
                                    .onItem().transformToUniAndMerge(
                pet ->
                    Uni.combine().all().unis(Uni.createFrom().item(pet),
                                             Uni.createFrom().publisher(toPublisher(petService.getCollarForMyPet(pet))))
                       .combinedWith(PetAndCollar::new)) // returns Publisher<PetAndCollar>
                                    .map(
                                        tuple ->
                                            PetConverter.addCollarToPet(tuple.pet, tuple.collar))); // returns Pet (with Collar)
    }

    static record PetAndCollar(Pet pet, Collar collar) {}

    static Publisher<Pet> searchPetsFunction(String petSearch) {
        return toFlowPublisher(Multi.createFrom().publisher(toPublisher(petRepository.searchPets(petSearch))) // returns Publisher<Pet>
            .onItem().transformToUniAndMerge(NestedMapsTest::addCollarToPet));
    }

    static Uni<Pet> addCollarToPet(Pet pet) {
        return Uni.createFrom().publisher(toPublisher(petService
            .getCollarForMyPet(pet))) // returns Uni<Collar>
            .map(
                collar ->
                    PetConverter.addCollarToPet(pet, collar)); // returns Pet (with Collar)
    }

    static Publisher<Pet> updatePets(String search) {
        return toFlowPublisher(
            Multi.createFrom().publisher(toPublisher(petRepository.searchPets(search))) // returns Publisher<Pet>
                 .onItem().transformToUniAndMerge(
                pet ->
                    Uni.combine().all().unis(Uni.createFrom().item(pet),
                                             Uni.createFrom().publisher(toPublisher(petService.getCollarForMyPet(pet))))
                       .combinedWith(PetAndCollar::new)) // returns Uni<PetAndCollar>
                 .map(
                     tuple ->
                         PetConverter.addCollarToPet(tuple.pet, tuple.collar))
                 .onItem().transformToUniAndMerge(
                pet ->
                    Uni.createFrom().publisher(toPublisher(petRepository.save(pet)))));
    }

    @ParameterizedTest
    @MethodSource("testArgs")
    void test(Function<String, Publisher<Pet>> searchPets) {
        AssertSubscriber<Pet> subscriber = Multi.createFrom().publisher(toPublisher(searchPets.apply("test"))).subscribe().withSubscriber(AssertSubscriber.create(3));

        subscriber.assertCompleted().assertItems(
            new Pet("dog with Collar of dog"),
            new Pet("cat with Collar of cat"),
            new Pet("bird with Collar of bird"));
    }

    static Stream<Arguments> testArgs() {
        return Stream.of(
            Arguments.of((Function<String, Publisher<Pet>>)NestedMapsTest::searchPetsNested),
            Arguments.of((Function<String, Publisher<Pet>>)NestedMapsTest::searchPetsZipTuple),
            Arguments.of((Function<String, Publisher<Pet>>)NestedMapsTest::searchPetsZipRecord),
            Arguments.of((Function<String, Publisher<Pet>>)NestedMapsTest::searchPetsFunction)
                        );
    }
}

