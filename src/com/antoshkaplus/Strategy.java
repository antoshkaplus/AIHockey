package com.antoshkaplus;

import com.antoshkaplus.model.Game;
import com.antoshkaplus.model.Hockeyist;
import com.antoshkaplus.model.Move;
import com.antoshkaplus.model.World;

/**
 * Стратегия --- интерфейс, содержащий описание методов искусственного интеллекта хоккеиста.
 * Каждая пользовательская стратегия должна реализовывать этот интерфейс.
 * Может отсутствовать в некоторых языковых пакетах, если язык не поддерживает интерфейсы.
 */
public interface Strategy {
    /**
     * Основной метод стратегии, осуществляющий управление хоккеистом.
     * Вызывается каждый тик для каждого хоккеиста.
     *
     * @param self  Хоккеист, которым данный метод будет осуществлять управление.
     * @param world Текущее состояние мира.
     * @param game  Различные игровые константы.
     * @param move  Результатом работы метода является изменение полей данного объекта.
     */
    void move(Hockeyist self, World world, Game game, Move move);
}
