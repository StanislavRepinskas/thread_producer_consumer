import java.util.Stack;

public class Main {
    public static String[] goodsList = new String[]{
            "Milk", "Water", "Bread", "Oil", "Meat", "Beer", "Potato", "Beans", "Sugar", "Chips", "Carrot", "Tomato"};

    public static String getRandomGoods() {
        return goodsList[getRandomInt(0, goodsList.length)];
    }

    public static int getRandomInt(int min, int max) {
        return min + (int) (Math.random() * max);
    }

    public static void main(String[] args) {
        int supplierCount = 3;
        Warehouse warehouse = new Warehouse(supplierCount);
        Thread[] threads = new Thread[]{
                new Supplier(warehouse, "BigC"),
                new Supplier(warehouse, "BestM"),
                new Supplier(warehouse, "SuperT"),
                new Worker(warehouse, "Jimmy"),
                new Worker(warehouse, "Billy"),
                //new Worker(warehouse, "Anna"),
        };

        try {
            for (Thread it : threads) {
                it.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("All goods has been delivered and received. Warehouse is closed!");
    }

    public static class Supplier extends Thread {
        private Warehouse warehouse;
        private String name;

        public Supplier(Warehouse warehouse, String name) {
            this.warehouse = warehouse;
            this.name = name;
            start();
        }

        @Override
        public void run() {
            int goodsCount = getRandomInt(1, 5);
            for (int i = 0; i < goodsCount; i++) {
                if (!warehouse.isWork())
                    break;

                try {
                    Thread.sleep(getRandomInt(2000, 5000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                warehouse.delivery(name, getRandomGoods());
            }
            warehouse.supplierLeave();
        }
    }

    public static class Worker extends Thread {
        private Warehouse warehouse;
        private String name;

        public Worker(Warehouse warehouse, String name) {
            this.warehouse = warehouse;
            this.name = name;
            start();
        }

        @Override
        public void run() {
            while (warehouse.isWork()) {
                String goods = warehouse.receive(name);
                if (goods == null)
                    break;
                try {
                    Thread.sleep(getRandomInt(2000, 5000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Worker [" + name + "] received the [" + goods + "]");
            }
        }
    }

    public static class Warehouse {
        private final Stack<String> goodsList = new Stack<>();
        private volatile boolean isWork = true;
        private volatile int supplierCount;

        public Warehouse(int supplierCount) {
            this.supplierCount = supplierCount;
        }

        public synchronized void supplierLeave() {
            supplierCount--;
            if (supplierCount <= 0)
                isWork = false;
        }

        public synchronized boolean isWork() {
            return isWork;
        }

        public void delivery(String supplier, String goods) {
            synchronized (goodsList) {
                goodsList.push(goods);
                System.out.println("Supplier [" + supplier + "] delivered the [" + goods + "]. Goods in stack: "
                        + goodsList.size());
                goodsList.notifyAll();
            }
        }

        public String receive(String worker) {
            synchronized (goodsList) {
                while (goodsList.isEmpty()) {
                    synchronized (this) {
                        if (supplierCount == 0)
                            return null;
                    }

                    System.out.println("Nothing to receive worker [" + worker + "] is wait...");
                    try {
                        goodsList.wait();
                        // Возможно товар уже взял кто то другой, что бы не показывать сообщение о том
                        // что работник ждет заморозим его опять.
                        if (goodsList.isEmpty())
                            goodsList.wait();
                        //System.out.println("Warehouse - worker " + worker + " awake " + System.currentTimeMillis());
                    } catch (InterruptedException e) {
                        System.out.println("Warehouse - receive worker [" + worker + "] interrupted");
                    }
                }

                String goods = goodsList.pop();
                System.out.println("Worker [" + worker + "] begin receiving the [" + goods + "]. Goods in stack: "
                        + goodsList.size());
                return goods;
            }
        }
    }
}
