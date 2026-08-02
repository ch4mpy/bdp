package com.c4soft.resthero.card.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.c4soft.resthero.card.domain.Card;
import com.c4soft.resthero.commons.domain.Iban;

interface JpaCardRepository extends JpaRepository<Card, String> {

  List<Card> findByIban(Iban iban);
}
