package com.edomingues.examples.mutiny;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.Flow;
import java.util.function.Function;
import java.util.stream.Stream;


class NestedMapsTest {

    static record Pet(String name) {}

    static record Collar(String tag) {}

    static class PetRepository {

        Multi<Pet> searchPets(String search) {
            return Multi.createFrom().items(new Pet("dog"), new Pet("cat"), new Pet("bird"));
        }

        Uni<Pet> save(Pet pet) {
            return Uni.createFrom().item(pet);
        }
    }

    static class PetService {

        Uni<Collar> getCollarForMyPet(Pet pet) {
            return Uni.createFrom().item(new Collar("Collar of " + pet.name));
        }

    }

    static class PetConverter {
        static Pet addCollarToPet(Pet pet, Collar collar) {
            return new Pet(pet.name + " with " + collar.tag);
        }
    }

    static PetRepository petRepository = new PetRepository();
    static PetService petService = new PetService();

    static Multi<Pet> searchPetsNested(String petSearch) {
        return petRepository
            .searchPets(petSearch) // returns Multi<Pet>
            .onItem().transformToUniAndMerge(pet ->
                         petService
                             .getCollarForMyPet(pet) // returns Uni<Collar>
                             .map(collar ->
                                      PetConverter.addCollarToPet(pet, collar) // returns Pet (with Collar)
                                 ));
    }

    static Multi<Pet> searchPetsZipTuple(String petSearch) {
        return petRepository
            .searchPets(petSearch) // returns Multi<Pet>
            .onItem().transformToUniAndMerge(
                pet ->
                    Uni.combine().all().unis(Uni.createFrom().item(pet),
                                             petService.getCollarForMyPet(pet))
                       .asTuple()) // returns Uni<Tuple<Pet,Collar>>
            .map(
                tuple ->
                    PetConverter.addCollarToPet(tuple.getItem1(), tuple.getItem2())); // returns Pet (with Collar)
    }

    static Multi<Pet> searchPetsZipRecord(String petSearch) {
        return petRepository
            .searchPets(petSearch) // returns Multi<Pet>
            .onItem().transformToUniAndMerge(
                pet ->
                    Uni.combine().all().unis(Uni.createFrom().item(pet),
                                             petService.getCollarForMyPet(pet))
                       .combinedWith(PetAndCollar::new)) // returns Uni<PetAndCollar>
            .map(
                tuple ->
                    PetConverter.addCollarToPet(tuple.pet, tuple.collar)); // returns Pet (with Collar)
    }

    static record PetAndCollar(Pet pet, Collar collar) {}

    static Multi<Pet> searchPetsFunction(String petSearch) {
        return petRepository
            .searchPets(petSearch) // returns Multi<Pet>
            .onItem().transformToUniAndMerge(NestedMapsTest::addCollarToPet);
    }

    static Uni<Pet> addCollarToPet(Pet pet) {
        return petService
            .getCollarForMyPet(pet) // returns Uni<Collar>
            .map(
                collar ->
                    PetConverter.addCollarToPet(pet, collar)); // returns Pet (with Collar)
    }

    static Multi<Pet> updatePets(String search) {
        return petRepository
            .searchPets(search)
            .onItem().transformToUniAndMerge(
                pet ->
                    Uni.combine().all().unis(Uni.createFrom().item(pet),
                                             petService.getCollarForMyPet(pet))
                       .combinedWith(PetAndCollar::new)) // returns Uni<PetAndCollar>
            .map(
                tuple -> PetConverter.addCollarToPet(tuple.pet, tuple.collar))
            .onItem().transformToUniAndMerge(petRepository::save);
    }

    @ParameterizedTest
    @MethodSource("testArgs")
    void test(Function<String, Multi<Pet>> searchPets) {
        AssertSubscriber<Pet> subscriber = searchPets.apply("test").subscribe().withSubscriber(AssertSubscriber.create(3));

        subscriber.assertCompleted().assertItems(
            new Pet("dog with Collar of dog"),
            new Pet("cat with Collar of cat"),
            new Pet("bird with Collar of bird"));
    }

    static Stream<Arguments> testArgs() {
        return Stream.of(
            Arguments.of((Function<String, Multi<Pet>>)NestedMapsTest::searchPetsNested),
            Arguments.of((Function<String, Multi<Pet>>)NestedMapsTest::searchPetsZipTuple),
            Arguments.of((Function<String, Multi<Pet>>)NestedMapsTest::searchPetsZipRecord),
            Arguments.of((Function<String, Multi<Pet>>)NestedMapsTest::searchPetsFunction)
                        );
    }
}

